package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DateFilterBar
import com.example.ui.components.FinalHisaabCard
import com.example.ui.components.LabHeader
import com.example.ui.components.PatientRowItem
import com.example.ui.components.SignatureSection
import com.example.ui.pdf.LabPdfGenerator
import com.example.ui.theme.BillBlue
import com.example.ui.theme.DiyaGreen
import com.example.ui.theme.LabDarkTeal
import com.example.ui.theme.LabPrimaryTeal
import com.example.ui.theme.LessOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.LabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LabHisaabScreen(
    viewModel: LabViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val insertionDate by viewModel.insertionDate.collectAsStateWithLifecycle()
    val filterStartDate by viewModel.filterStartDate.collectAsStateWithLifecycle()
    val filterEndDate by viewModel.filterEndDate.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val filteredPatients by viewModel.filteredPatients.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    fun formatInr(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    fun formatDisplayDate(dateStr: String): String {
        val parts = dateStr.split("-")
        return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
    }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Export PDF handler
    fun handleDownloadPdf() {
        if (isGeneratingPdf) return
        isGeneratingPdf = true
        coroutineScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    LabPdfGenerator.generatePdfFile(
                        context = context,
                        patients = filteredPatients,
                        settings = settings,
                        startDate = filterStartDate,
                        endDate = filterEndDate,
                        todayDate = todayStr
                    )
                }
                snackbarHostState.showSnackbar("PDF Taiyar ho gaya! Share dialog khul raha hai...")
                LabPdfGenerator.sharePdf(context, file)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("PDF banane mein samasya: ${e.localizedMessage}")
            } finally {
                isGeneratingPdf = false
            }
        }
    }

    // Print handler
    fun handlePrint() {
        if (isGeneratingPdf) return
        isGeneratingPdf = true
        coroutineScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    LabPdfGenerator.generatePdfFile(
                        context = context,
                        patients = filteredPatients,
                        settings = settings,
                        startDate = filterStartDate,
                        endDate = filterEndDate,
                        todayDate = todayStr
                    )
                }
                LabPdfGenerator.printPdf(context, file)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Print error: ${e.localizedMessage}")
            } finally {
                isGeneratingPdf = false
            }
        }
    }

    // Share text summary
    fun handleShareWhatsApp() {
        LabPdfGenerator.shareWhatsAppSummary(
            context = context,
            patients = filteredPatients,
            startDate = filterStartDate,
            endDate = filterEndDate,
            todayDate = todayStr
        )
    }

    // Group patients by date
    val groupedPatients = remember(filteredPatients) {
        filteredPatients.groupBy { it.date }.toSortedMap()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Sticky Bottom Summary Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sticky_bottom_summary"),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Patient", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("${summary.totalPatients}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kul Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(formatInr(summary.totalBill), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BillBlue)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kul Less", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(formatInr(summary.totalLess), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = LessOrange)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kul Diya", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(formatInr(summary.totalPaid), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DiyaGreen)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                Spacer(modifier = Modifier.height(6.dp))
                LabHeader(
                    onDownloadPdf = { handleDownloadPdf() },
                    onPrint = { handlePrint() },
                    onShareSummary = { handleShareWhatsApp() }
                )
            }

            item(key = "date_filter") {
                DateFilterBar(
                    insertionDate = insertionDate,
                    filterStartDate = filterStartDate,
                    filterEndDate = filterEndDate,
                    searchQuery = searchQuery,
                    onInsertionDateChanged = { viewModel.setInsertionDate(it) },
                    onDateRangeChanged = { start, end -> viewModel.setDateFilter(start, end) },
                    onClearFilter = { viewModel.clearDateFilter() },
                    onSearchQueryChanged = { viewModel.setSearchQuery(it) }
                )
            }

            // Table Column Headers: # | Patient ka naam | Bill ₹ | Less ₹ | Diya ₹
            item(key = "table_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "#",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.width(20.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Patient ka naam",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1.3f)
                    )
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BillBlue)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bill ₹", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(LessOrange)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Less ₹", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DiyaGreen)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Diya ₹", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.width(28.dp)) // Space for delete icon
                }
            }

            // Patients list grouped by Date
            if (groupedPatients.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Is range mein koi patient record nahi mila.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Naya patient jodane ke liye neeche button dabayein.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                groupedPatients.forEach { (groupDate, patientsInGroup) ->
                    // Group Header Banner
                    item(key = "group_header_$groupDate") {
                        val groupBill = patientsInGroup.sumOf { it.billAmount }
                        val groupPaid = patientsInGroup.sumOf { it.paidAmount }
                        val groupCount = patientsInGroup.count { it.name.isNotBlank() }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 ${formatDisplayDate(groupDate)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LabDarkTeal
                                )
                                Text(
                                    text = "$groupCount patient · Bill ${formatInr(groupBill)} · Diya ${formatInr(groupPaid)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // Patients under this date
                    itemsIndexed(
                        items = patientsInGroup,
                        key = { _, patient -> patient.id }
                    ) { idx, patient ->
                        PatientRowItem(
                            index = idx + 1,
                            patient = patient,
                            onNameChange = { viewModel.updatePatientName(patient, it) },
                            onCareOfChange = { viewModel.updatePatientCareOf(patient, it) },
                            onBillChange = { viewModel.updatePatientBill(patient, it) },
                            onLessChange = { viewModel.updatePatientLess(patient, it) },
                            onPaidChange = { viewModel.updatePatientPaid(patient, it) },
                            onDelete = { viewModel.deletePatient(patient.id) }
                        )
                    }
                }
            }

            // "+ Naya patient" Button
            item(key = "add_patient_button") {
                Button(
                    onClick = { viewModel.addNewPatient() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_patient_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LabPrimaryTeal,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add patient",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Naya patient",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Final Hisaab Card
            item(key = "final_hisaab") {
                FinalHisaabCard(summary = summary)
            }

            // Signature & Verification Section
            item(key = "signatures") {
                SignatureSection(
                    settings = settings,
                    onPreparedByNameChange = { viewModel.updatePreparedByName(it) },
                    onPreparedBySignatureChange = { viewModel.updatePreparedBySignature(it) },
                    onPaidByNameChange = { viewModel.updatePaidByName(it) },
                    onPaidBySignatureChange = { viewModel.updatePaidBySignature(it) },
                    onToggleMarkedPaid = { viewModel.toggleMarkedPaid(it) }
                )
            }

            // Bottom Actions (PDF & Print)
            item(key = "bottom_links") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { handleDownloadPdf() }) {
                        Text(
                            text = "⬇ PDF Download",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = { handlePrint() }) {
                        Text(
                            text = "🖨 Print",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            // "Nayi list shuru karo" (Reset with confirmation)
            item(key = "reset_list") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.testTag("reset_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nayi list shuru karo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for Reset
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Pakka? Nayi list shuru karni hai?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Saare patient ka hisaab aur signatures saaf ho jaayenge. Kya aap aage badhna chahte hain?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllList()
                        showResetDialog = false
                        Toast.makeText(context, "List saaf ho gayi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Haan, Saaf Karo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Nahi, Cancel")
                }
            }
        )
    }

    // PDF generation loading indicator
    if (isGeneratingPdf) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = LabPrimaryTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "PDF taiyar ho raha hai...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
