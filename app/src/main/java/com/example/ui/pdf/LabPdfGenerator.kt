package com.example.ui.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.model.LabSettings
import com.example.data.model.PatientRecord
import com.example.ui.signature.SignatureSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object LabPdfGenerator {

    private fun formatInr(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    private fun formatNum(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    private fun formatDisplayDate(dateStr: String): String {
        val parts = dateStr.split("-")
        return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
    }

    fun generatePdfFile(
        context: Context,
        patients: List<PatientRecord>,
        settings: LabSettings,
        startDate: String,
        endDate: String,
        todayDate: String
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Sharda-Lab-Hisaab-${todayDate}.pdf"
        val pdfFile = File(reportsDir, fileName)

        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 35f
        val marginRight = 560f
        val contentWidth = marginRight - marginLeft

        val paint = Paint().apply { isAntiAlias = true }

        val filtered = patients.filter {
            it.name.isNotBlank() || it.billAmount > 0 || it.lessAmount > 0 || it.paidAmount > 0
        }
        val groupedByDate = filtered.groupBy { it.date }.toSortedMap()

        // Totals
        val totalPatients = filtered.count { it.name.isNotBlank() }
        val totalBill = filtered.sumOf { it.billAmount }
        val totalLess = filtered.sumOf { it.lessAmount }
        val totalPaid = filtered.sumOf { it.paidAmount }
        val lessPct = if (totalBill > 0) String.format(Locale.US, "%.1f%%", (totalLess / totalBill) * 100) else "0.0%"
        val paidPct = if (totalBill > 0) String.format(Locale.US, "%.1f%%", (totalPaid / totalBill) * 100) else "0.0%"

        // Column widths
        // 0: # (24), 1: Patient ka naam (220), 2: Bill (70), 3: Less (70), 4: Less % (55), 5: Diya (86)
        val colWidths = floatArrayOf(26f, 230f, 68f, 68f, 55f, 78f)
        val colAligns = arrayOf(Paint.Align.LEFT, Paint.Align.LEFT, Paint.Align.RIGHT, Paint.Align.RIGHT, Paint.Align.RIGHT, Paint.Align.RIGHT)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeaderBand(yTop: Float, isCompact: Boolean) {
            paint.color = Color.parseColor("#0F3D3A")
            paint.style = Paint.Style.FILL
            val h = if (isCompact) 26f else 54f
            canvas.drawRect(0f, yTop, pageWidth.toFloat(), yTop + h, paint)

            // Droplet logo badge
            paint.color = Color.WHITE
            val badgeX = marginLeft + 18f
            val badgeY = yTop + h / 2f
            canvas.drawCircle(badgeX, badgeY, if (isCompact) 10f else 18f, paint)

            // Red drop
            paint.color = Color.parseColor("#C0392B")
            canvas.drawCircle(badgeX, badgeY, if (isCompact) 7f else 12f, paint)
            // White cross
            paint.color = Color.WHITE
            paint.strokeWidth = 2.5f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(badgeX - 4f, badgeY, badgeX + 4f, badgeY, paint)
            canvas.drawLine(badgeX, badgeY - 4f, badgeX, badgeY + 4f, paint)
            paint.style = Paint.Style.FILL

            if (isCompact) {
                paint.color = Color.WHITE
                paint.textSize = 10.5f
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("SHARDA PATHOLOGICAL LAB – Patient Hisaab List", pageWidth / 2f, yTop + 17f, paint)
            } else {
                paint.color = Color.WHITE
                paint.textSize = 18f
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("SHARDA PATHOLOGICAL LAB", pageWidth / 2f, yTop + 25f, paint)

                paint.color = Color.parseColor("#CFE8E4")
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("Patient Hisaab List", pageWidth / 2f, yTop + 43f, paint)
            }
        }

        fun drawTableHead(y: Float) {
            paint.color = Color.parseColor("#DFE7E6")
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginLeft, y - 12f, marginRight, y + 8f, paint)

            paint.color = Color.parseColor("#555555")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRect(marginLeft, y - 12f, marginRight, y + 8f, paint)
            paint.style = Paint.Style.FILL

            val headers = arrayOf("#", "Patient ka naam", "Bill (Rs)", "Less (Rs)", "Less %", "Diya (Rs)")
            val headerColors = intArrayOf(
                Color.BLACK,
                Color.BLACK,
                Color.parseColor("#2563EB"),
                Color.parseColor("#EA580C"),
                Color.parseColor("#EA580C"),
                Color.parseColor("#16A34A")
            )

            var cx = marginLeft
            for (i in headers.indices) {
                val cw = colWidths[i]
                if (i >= 2) {
                    paint.color = headerColors[i]
                    canvas.drawRect(cx, y - 12f, cx + cw, y + 8f, paint)
                }
                paint.color = if (i >= 2) Color.WHITE else Color.BLACK
                paint.textSize = 10f
                paint.isFakeBoldText = true
                paint.textAlign = colAligns[i]
                val textX = if (colAligns[i] == Paint.Align.LEFT) cx + 4f else cx + cw - 4f
                canvas.drawText(headers[i], textX, y + 2f, paint)
                cx += cw
            }
        }

        // Draw First Page Header
        drawHeaderBand(0f, false)

        var curY = 70f
        paint.color = Color.DKGRAY
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        val dateSub = if (startDate.isNotBlank() || endDate.isNotBlank()) {
            "Tareekh: ${if (startDate.isNotBlank()) formatDisplayDate(startDate) else "…"} se ${if (endDate.isNotBlank()) formatDisplayDate(endDate) else "…"} tak"
        } else {
            "Print tareekh: ${formatDisplayDate(todayDate)}"
        }
        canvas.drawText(dateSub, pageWidth / 2f, curY, paint)

        curY = 90f
        drawTableHead(curY)
        curY += 24f

        fun checkNewPage(neededHeight: Float) {
            if (curY + neededHeight > pageHeight - 35f) {
                // Draw page number
                paint.color = Color.GRAY
                paint.textSize = 9f
                paint.textAlign = Paint.Align.CENTER
                paint.isFakeBoldText = false
                canvas.drawText("Page $pageNumber", pageWidth / 2f, pageHeight - 15f, paint)

                doc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas

                drawHeaderBand(0f, true)
                curY = 44f
                drawTableHead(curY)
                curY += 24f
            }
        }

        // Render groups
        for ((groupDate, groupPatients) in groupedByDate) {
            checkNewPage(45f)

            // Date Group Header Banner
            paint.color = Color.parseColor("#CFE6E2")
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginLeft, curY - 11f, marginRight, curY + 6f, paint)
            paint.color = Color.parseColor("#0F3D3A")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRect(marginLeft, curY - 11f, marginRight, curY + 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#0F3D3A")
            paint.textSize = 10.5f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Tareekh: ${formatDisplayDate(groupDate)}", marginLeft + 6f, curY + 1f, paint)
            curY += 19f

            groupPatients.forEachIndexed { idx, p ->
                checkNewPage(20f)

                // Background striping
                if (idx % 2 == 1) {
                    paint.color = Color.parseColor("#F8FAFC")
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(marginLeft, curY - 11f, marginRight, curY + 6f, paint)
                }

                // Tint for amounts
                val bgTints = intArrayOf(
                    0, 0,
                    Color.parseColor("#EFF6FF"),
                    Color.parseColor("#FFF7ED"),
                    Color.parseColor("#FFF7ED"),
                    Color.parseColor("#F0FDF4")
                )
                var colX = marginLeft
                for (ci in 2..5) {
                    val cw = colWidths[ci]
                    val offX = (0 until ci).sumOf { colWidths[it].toDouble() }.toFloat()
                    paint.color = bgTints[ci]
                    canvas.drawRect(marginLeft + offX, curY - 11f, marginLeft + offX + cw, curY + 6f, paint)
                }

                // Row content
                val nameWithCo = if (p.careOf.isNotBlank()) "${p.name.ifBlank { "-" }} (C/O ${p.careOf})" else p.name.ifBlank { "-" }
                val rowValues = arrayOf(
                    "${idx + 1}",
                    nameWithCo,
                    formatNum(p.billAmount),
                    formatNum(p.lessAmount),
                    if (p.billAmount > 0) String.format(Locale.US, "%.1f%%", p.lessPercentage) else "–",
                    formatNum(p.paidAmount)
                )

                var rx = marginLeft
                for (ci in rowValues.indices) {
                    val cw = colWidths[ci]
                    paint.color = when (ci) {
                        2 -> Color.parseColor("#1D4ED8")
                        3, 4 -> Color.parseColor("#C2410C")
                        5 -> Color.parseColor("#15803D")
                        else -> Color.BLACK
                    }
                    paint.textSize = if (ci in 2..5) 10.5f else 10f
                    paint.isFakeBoldText = ci in 2..5
                    paint.textAlign = colAligns[ci]
                    val tx = if (colAligns[ci] == Paint.Align.LEFT) rx + 4f else rx + cw - 4f
                    // Truncate name if too long
                    val textToDraw = if (ci == 1 && nameWithCo.length > 32) nameWithCo.take(30) + "…" else rowValues[ci]
                    canvas.drawText(textToDraw, tx, curY + 1f, paint)
                    rx += cw
                }

                // Horizontal dividing line
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(marginLeft, curY + 6f, marginRight, curY + 6f, paint)
                paint.style = Paint.Style.FILL

                curY += 18f
            }

            // Group Subtotal Row
            checkNewPage(24f)
            val groupBill = groupPatients.sumOf { it.billAmount }
            val groupLess = groupPatients.sumOf { it.lessAmount }
            val groupPaid = groupPatients.sumOf { it.paidAmount }
            val grpLessPct = if (groupBill > 0) String.format(Locale.US, "%.1f%%", (groupLess / groupBill) * 100) else "–"

            paint.color = Color.parseColor("#EAF1F0")
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginLeft, curY - 11f, marginRight, curY + 6f, paint)

            paint.color = Color.parseColor("#0F3D3A")
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Is din ka total", marginLeft + colWidths[0] + 4f, curY + 1f, paint)

            val subValues = arrayOf(formatNum(groupBill), formatNum(groupLess), grpLessPct, formatNum(groupPaid))
            var sx = marginLeft + colWidths[0] + colWidths[1]
            for (ci in subValues.indices) {
                val cw = colWidths[ci + 2]
                paint.color = when (ci) {
                    0 -> Color.parseColor("#1D4ED8")
                    1, 2 -> Color.parseColor("#C2410C")
                    else -> Color.parseColor("#15803D")
                }
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(subValues[ci], sx + cw - 4f, curY + 1f, paint)
                sx += cw
            }
            curY += 26f
        }

        // Final Hisaab Summary Card
        checkNewPage(190f)

        paint.color = Color.parseColor("#0F3D3A")
        paint.style = Paint.Style.FILL
        canvas.drawRect(marginLeft, curY, marginRight, curY + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("FINAL HISAAB", marginLeft + 10f, curY + 15f, paint)

        val summaryItems = listOf(
            Triple("Kul patient", "$totalPatients", Color.BLACK),
            Triple("Kul Bill", formatInr(totalBill), Color.parseColor("#1D4ED8")),
            Triple("Kul Less", "${formatInr(totalLess)}  ($lessPct)", Color.parseColor("#C2410C")),
            Triple("Kul Diya", "${formatInr(totalPaid)}  ($paidPct)", Color.parseColor("#15803D"))
        )

        var sTop = curY + 22f
        summaryItems.forEachIndexed { i, (label, value, col) ->
            paint.color = Color.parseColor("#F8FAFC")
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginLeft, sTop, marginRight, sTop + 20f, paint)

            paint.color = Color.BLACK
            paint.textSize = 10.5f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, marginLeft + 10f, sTop + 14f, paint)

            paint.color = col
            paint.textSize = 11f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, marginRight - 10f, sTop + 14f, paint)

            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(marginLeft, sTop + 20f, marginRight, sTop + 20f, paint)
            paint.style = Paint.Style.FILL

            sTop += 20f
        }

        paint.color = Color.parseColor("#555555")
        paint.strokeWidth = 0.8f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(marginLeft, curY, marginRight, sTop, paint)
        paint.style = Paint.Style.FILL

        curY = sTop + 12f

        // "Ye total bill PAID ho chuka hai" banner
        if (settings.isMarkedPaid) {
            paint.color = Color.parseColor("#D9F0DF")
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginLeft, curY, marginRight, curY + 22f, paint)

            paint.color = Color.parseColor("#1E8449")
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawRect(marginLeft, curY, marginRight, curY + 22f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#14532D")
            paint.textSize = 11f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("✓ Ye total bill PAID ho chuka hai.", pageWidth / 2f, curY + 15f, paint)
            curY += 34f
        } else {
            curY += 10f
        }

        // Signature Boxes
        checkNewPage(90f)

        val boxWidth = 220f
        val boxHeight = 55f
        val box1Left = marginLeft
        val box2Left = marginRight - boxWidth

        // Box 1 Label
        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Hisaab banane wale ka signature", box1Left, curY - 4f, paint)

        // Box 2 Label
        canvas.drawText("Paid signature (paisa mil gaya)", box2Left, curY - 4f, paint)

        // Draw Box 1 container
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(box1Left, curY, box1Left + boxWidth, curY + boxHeight, paint)
        paint.color = Color.parseColor("#666666")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRect(box1Left, curY, box1Left + boxWidth, curY + boxHeight, paint)

        // Draw Box 2 container
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(box2Left, curY, box2Left + boxWidth, curY + boxHeight, paint)
        paint.color = Color.parseColor("#666666")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRect(box2Left, curY, box2Left + boxWidth, curY + boxHeight, paint)
        paint.style = Paint.Style.FILL

        // Draw Signatures bitmaps if available
        val strokes1 = SignatureSerializer.deserialize(settings.preparedBySignaturePoints)
        if (strokes1.isNotEmpty()) {
            val bm1 = SignatureSerializer.createBitmap(strokes1, boxWidth.toInt() * 2, boxHeight.toInt() * 2)
            canvas.drawBitmap(bm1, null, RectF(box1Left + 2f, curY + 2f, box1Left + boxWidth - 2f, curY + boxHeight - 2f), null)
        }

        val strokes2 = SignatureSerializer.deserialize(settings.paidBySignaturePoints)
        if (strokes2.isNotEmpty()) {
            val bm2 = SignatureSerializer.createBitmap(strokes2, boxWidth.toInt() * 2, boxHeight.toInt() * 2)
            canvas.drawBitmap(bm2, null, RectF(box2Left + 2f, curY + 2f, box2Left + boxWidth - 2f, curY + boxHeight - 2f), null)
        }

        // Signer Names
        if (settings.preparedByName.isNotBlank()) {
            paint.color = Color.BLACK
            paint.textSize = 10f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Naam: ${settings.preparedByName}", box1Left, curY + boxHeight + 14f, paint)
        }

        if (settings.paidByName.isNotBlank()) {
            paint.color = Color.BLACK
            paint.textSize = 10f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Naam: ${settings.paidByName}", box2Left, curY + boxHeight + 14f, paint)
        }

        // Draw final page number
        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = false
        canvas.drawText("Page $pageNumber", pageWidth / 2f, pageHeight - 15f, paint)

        doc.finishPage(page)

        FileOutputStream(pdfFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return pdfFile
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sharda Pathological Lab - Patient Hisaab")
            putExtra(Intent.EXTRA_TEXT, "Sharda Pathological Lab Hisaab List PDF")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Hisaab PDF"))
    }

    fun printPdf(context: Context, pdfFile: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (destination == null) return
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print("Sharda_Lab_Hisaab", printAdapter, PrintAttributes.Builder().build())
    }

    fun shareWhatsAppSummary(
        context: Context,
        patients: List<PatientRecord>,
        startDate: String,
        endDate: String,
        todayDate: String
    ) {
        val filtered = patients.filter { it.name.isNotBlank() || it.billAmount > 0 }
        val count = filtered.count { it.name.isNotBlank() }
        val bill = filtered.sumOf { it.billAmount }
        val less = filtered.sumOf { it.lessAmount }
        val paid = filtered.sumOf { it.paidAmount }
        val due = maxOf(0.0, bill - less - paid)

        val text = buildString {
            appendLine("🏥 *Sharda Pathological Lab*")
            appendLine("📋 *Patient Hisaab List*")
            if (startDate.isNotBlank() || endDate.isNotBlank()) {
                appendLine("📅 Tareekh: ${formatDisplayDate(startDate)} se ${formatDisplayDate(endDate)}")
            } else {
                appendLine("📅 Tareekh: ${formatDisplayDate(todayDate)}")
            }
            appendLine("─────────────────")
            appendLine("👤 Kul Patient: $count")
            appendLine("💵 Kul Bill: ${formatInr(bill)}")
            appendLine("🏷️ Kul Less: ${formatInr(less)}")
            appendLine("✅ Kul Diya: ${formatInr(paid)}")
            if (due > 0) {
                appendLine("⚠️ Baaki Due: ${formatInr(due)}")
            }
            appendLine("─────────────────")
            appendLine("Ye total hisaab verified hai.")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Hisaab Share Karein"))
    }
}
