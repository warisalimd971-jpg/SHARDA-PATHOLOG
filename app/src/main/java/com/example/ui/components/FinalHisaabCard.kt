package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BillBlueDark
import com.example.ui.theme.DiyaGreenDark
import com.example.ui.theme.LabDarkTeal
import com.example.ui.theme.LessOrangeDark
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.LabUiSummary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinalHisaabCard(
    modifier: Modifier = Modifier,
    summary: LabUiSummary
) {
    fun formatInr(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("final_hisaab_card")
    ) {
        Text(
            text = "FINAL HISAAB",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LabDarkTeal,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Kul patient
        SummaryLine(
            title = "Kul patient",
            value = "${summary.totalPatients}",
            valueColor = TextPrimary
        )

        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

        // Kul Bill
        SummaryLine(
            title = "Kul Bill",
            value = formatInr(summary.totalBill),
            valueColor = BillBlueDark
        )

        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

        // Kul Less
        val lessPct = if (summary.totalBill > 0) String.format(Locale.US, "%.1f%%", summary.lessPercentage) else "0.0%"
        SummaryLine(
            title = "Kul Less (Bill ka %)",
            value = "${formatInr(summary.totalLess)} ($lessPct)",
            valueColor = LessOrangeDark
        )

        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

        // Kul Diya
        val paidPct = if (summary.totalBill > 0) String.format(Locale.US, "%.1f%%", summary.paidPercentage) else "0.0%"
        SummaryLine(
            title = "Kul Diya (Bill ka %)",
            value = "${formatInr(summary.totalPaid)} ($paidPct)",
            valueColor = DiyaGreenDark
        )

        // Kul Due if any
        if (summary.totalDue > 0) {
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
            SummaryLine(
                title = "Kul Baaki (Pending Due)",
                value = formatInr(summary.totalDue),
                valueColor = Color(0xFFDC2626)
            )
        }
    }
}

@Composable
private fun SummaryLine(
    title: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}
