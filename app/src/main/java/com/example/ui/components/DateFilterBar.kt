package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LabDarkTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateFilterBar(
    modifier: Modifier = Modifier,
    insertionDate: String,
    filterStartDate: String,
    filterEndDate: String,
    searchQuery: String,
    onInsertionDateChanged: (String) -> Unit,
    onDateRangeChanged: (start: String, end: String) -> Unit,
    onClearFilter: () -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showDatePicker(initialDate: String, onDateSelected: (String) -> Unit) {
        val parts = initialDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH)
        val d = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formatted)
        }, y, m, d).show()
    }

    fun formatDisplayDate(dateStr: String): String {
        if (dateStr.isBlank()) return "Chuno"
        val parts = dateStr.split("-")
        return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
    }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Today / Target insertion date
        Text(
            text = "📅 Aaj ki tareekh (naye patient isi din mein judenge)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
                .clickable {
                    showDatePicker(insertionDate) { onInsertionDateChanged(it) }
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("insertion_date_selector")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatDisplayDate(insertionDate),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LabDarkTeal
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select date",
                    tint = LabDarkTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Date Range: "Hisaab dekho: se [Date] tak [Date]"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hisaab dekho: se",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .clickable {
                            showDatePicker(filterStartDate.ifBlank { todayStr }) {
                                onDateRangeChanged(it, filterEndDate)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .testTag("filter_start_date")
                ) {
                    Text(
                        text = if (filterStartDate.isBlank()) "Sabhi" else formatDisplayDate(filterStartDate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (filterStartDate.isBlank()) TextMuted else TextPrimary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "tak",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .clickable {
                            showDatePicker(filterEndDate.ifBlank { todayStr }) {
                                onDateRangeChanged(filterStartDate, it)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .testTag("filter_end_date")
                ) {
                    Text(
                        text = if (filterEndDate.isBlank()) "Sabhi" else formatDisplayDate(filterEndDate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (filterEndDate.isBlank()) TextMuted else TextPrimary
                    )
                }
            }
        }

        // Quick Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isAll = filterStartDate.isBlank() && filterEndDate.isBlank()
            val isToday = filterStartDate == todayStr && filterEndDate == todayStr

            FilterChip(
                selected = isAll,
                onClick = onClearFilter,
                label = { Text("Sabhi", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )

            FilterChip(
                selected = isToday,
                onClick = { onDateRangeChanged(todayStr, todayStr) },
                label = { Text("Aaj", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )

            if (!isAll) {
                FilterChip(
                    selected = false,
                    onClick = onClearFilter,
                    label = { Text("Hatao ✕", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Search Bar for patient name or C/O
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("patient_search_input"),
            placeholder = { Text("Patient ka naam ya C/O khojein...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LabDarkTeal,
                unfocusedBorderColor = BorderSubtle
            )
        )
    }
}
