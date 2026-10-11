package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_records")
data class PatientRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val name: String = "",
    val careOf: String = "", // C/O doctor or relative
    val billAmount: Double = 0.0,
    val lessAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val isManualPaid: Boolean = false, // If true, user manually typed paid amount
    val timestamp: Long = System.currentTimeMillis()
) {
    val dueAmount: Double
        get() = maxOf(0.0, billAmount - lessAmount - paidAmount)

    val lessPercentage: Double
        get() = if (billAmount > 0.0) (lessAmount / billAmount) * 100.0 else 0.0

    val paidPercentage: Double
        get() = if (billAmount > 0.0) (paidAmount / billAmount) * 100.0 else 0.0
}
