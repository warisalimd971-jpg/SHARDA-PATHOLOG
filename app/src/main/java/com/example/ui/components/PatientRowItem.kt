package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientRecord
import com.example.ui.theme.BillBlue
import com.example.ui.theme.BillBlueBg
import com.example.ui.theme.DiyaGreen
import com.example.ui.theme.DiyaGreenBg
import com.example.ui.theme.LessOrange
import com.example.ui.theme.LessOrangeBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun PatientRowItem(
    modifier: Modifier = Modifier,
    index: Int,
    patient: PatientRecord,
    onNameChange: (String) -> Unit,
    onCareOfChange: (String) -> Unit,
    onBillChange: (Double) -> Unit,
    onLessChange: (Double) -> Unit,
    onPaidChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var billText by remember(patient.billAmount) {
        mutableStateOf(if (patient.billAmount > 0) patient.billAmount.toInt().toString() else "")
    }
    var lessText by remember(patient.lessAmount) {
        mutableStateOf(if (patient.lessAmount > 0) patient.lessAmount.toInt().toString() else "")
    }
    var paidText by remember(patient.paidAmount) {
        mutableStateOf(if (patient.paidAmount > 0) patient.paidAmount.toInt().toString() else "")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("patient_row_${patient.id}")
    ) {
        // Main Row: # | Name Input | Bill ₹ | Less ₹ | Diya ₹ | Delete (✕)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // # Serial
            Text(
                text = "$index",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.width(20.dp),
                textAlign = TextAlign.Center
            )

            // Patient Name Input
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (patient.name.isEmpty()) {
                    Text(
                        text = "Naam",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = patient.name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_name_input_${patient.id}")
                )
            }

            // Bill ₹ Input
            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BillBlueBg)
                    .border(1.5.dp, BillBlue, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (billText.isEmpty()) {
                    Text(
                        text = "0",
                        color = BillBlue.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BasicTextField(
                    value = billText,
                    onValueChange = { newVal ->
                        val filtered = newVal.filter { it.isDigit() }
                        billText = filtered
                        onBillChange(filtered.toDoubleOrNull() ?: 0.0)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Right
                    ),
                    cursorBrush = SolidColor(BillBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_bill_input_${patient.id}")
                )
            }

            // Less ₹ Input
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(LessOrangeBg)
                    .border(1.5.dp, LessOrange, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (lessText.isEmpty()) {
                    Text(
                        text = "0",
                        color = LessOrange.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BasicTextField(
                    value = lessText,
                    onValueChange = { newVal ->
                        val filtered = newVal.filter { it.isDigit() }
                        lessText = filtered
                        onLessChange(filtered.toDoubleOrNull() ?: 0.0)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Right
                    ),
                    cursorBrush = SolidColor(LessOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_less_input_${patient.id}")
                )
            }

            // Diya ₹ Input (Auto / Manual)
            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DiyaGreenBg)
                    .border(1.5.dp, DiyaGreen, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (paidText.isEmpty()) {
                    Text(
                        text = "auto",
                        color = DiyaGreen.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BasicTextField(
                    value = paidText,
                    onValueChange = { newVal ->
                        val filtered = newVal.filter { it.isDigit() }
                        paidText = filtered
                        onPaidChange(filtered.toDoubleOrNull() ?: 0.0)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Right
                    ),
                    cursorBrush = SolidColor(DiyaGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_paid_input_${patient.id}")
                )
            }

            // Delete ✕ Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_patient_${patient.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete row",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sub Row: C/O (Care of) + Less % Badge + Baaki alert if due
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // C/O input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (patient.careOf.isEmpty()) {
                    Text(
                        text = "C/O (care of doctor/relative)",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                BasicTextField(
                    value = patient.careOf,
                    onValueChange = onCareOfChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_care_of_input_${patient.id}")
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badges: Less % and Baaki (due)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (patient.billAmount > 0 && patient.lessAmount > 0) {
                    Text(
                        text = "Less ${String.format(Locale.US, "%.1f%%", patient.lessPercentage)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = LessOrange
                    )
                }

                if (patient.dueAmount > 0) {
                    Text(
                        text = "Baaki ₹${patient.dueAmount.toInt()}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}
