package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LabSettings
import com.example.ui.signature.SignaturePad
import com.example.ui.signature.SignatureSerializer
import com.example.ui.signature.StrokePath
import com.example.ui.theme.PaidBannerBg
import com.example.ui.theme.PaidBannerBorder
import com.example.ui.theme.PaidBannerText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SignatureSection(
    modifier: Modifier = Modifier,
    settings: LabSettings,
    onPreparedByNameChange: (String) -> Unit,
    onPreparedBySignatureChange: (String) -> Unit,
    onPaidByNameChange: (String) -> Unit,
    onPaidBySignatureChange: (String) -> Unit,
    onToggleMarkedPaid: (Boolean) -> Unit
) {
    val preparedStrokes = remember(settings.preparedBySignaturePoints) {
        val list = mutableStateListOf<StrokePath>()
        list.addAll(SignatureSerializer.deserialize(settings.preparedBySignaturePoints))
        list
    }

    val paidStrokes = remember(settings.paidBySignaturePoints) {
        val list = mutableStateListOf<StrokePath>()
        list.addAll(SignatureSerializer.deserialize(settings.paidBySignaturePoints))
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("signature_section")
    ) {
        // "Ye total bill PAID ho chuka hai" Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (settings.isMarkedPaid) PaidBannerBg else Color(0xFFFEF2F2))
                .border(
                    width = 1.5.dp,
                    color = if (settings.isMarkedPaid) PaidBannerBorder else Color(0xFFEF4444),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onToggleMarkedPaid(!settings.isMarkedPaid) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("paid_status_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (settings.isMarkedPaid) PaidBannerText else Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (settings.isMarkedPaid) "Ye total bill PAID ho chuka hai" else "Bill PAID nahi hua (Unpaid)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (settings.isMarkedPaid) PaidBannerText else Color(0xFF991B1B)
                    )
                }

                Switch(
                    checked = settings.isMarkedPaid,
                    onCheckedChange = onToggleMarkedPaid,
                    modifier = Modifier.size(width = 40.dp, height = 24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PaidBannerBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Signature 1: Hisaab banane wale ka signature
        Text(
            text = "Hisaab banane wale ka signature",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        SignaturePad(
            modifier = Modifier.fillMaxWidth(),
            strokes = preparedStrokes,
            onStrokeAdded = { stroke ->
                preparedStrokes.add(stroke)
                onPreparedBySignatureChange(SignatureSerializer.serialize(preparedStrokes.toList()))
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = settings.preparedByName,
                onValueChange = onPreparedByNameChange,
                placeholder = { Text("Hisaab banane wale ka naam", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("prepared_by_name_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            OutlinedButton(
                onClick = {
                    preparedStrokes.clear()
                    onPreparedBySignatureChange("")
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("clear_prepared_sig_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Saaf", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signature 2: Paid signature (paisa mil gaya)
        Text(
            text = "Paid signature (paisa mil gaya)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        SignaturePad(
            modifier = Modifier.fillMaxWidth(),
            strokes = paidStrokes,
            onStrokeAdded = { stroke ->
                paidStrokes.add(stroke)
                onPaidBySignatureChange(SignatureSerializer.serialize(paidStrokes.toList()))
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = settings.paidByName,
                onValueChange = onPaidByNameChange,
                placeholder = { Text("Paisa lene/dene wale ka naam", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("paid_by_name_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            OutlinedButton(
                onClick = {
                    paidStrokes.clear()
                    onPaidBySignatureChange("")
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("clear_paid_sig_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Saaf", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
