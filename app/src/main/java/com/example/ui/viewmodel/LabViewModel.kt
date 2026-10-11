package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LabDatabase
import com.example.data.model.LabSettings
import com.example.data.model.PatientRecord
import com.example.data.repository.LabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LabUiSummary(
    val totalPatients: Int = 0,
    val totalBill: Double = 0.0,
    val totalLess: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDue: Double = 0.0,
    val lessPercentage: Double = 0.0,
    val paidPercentage: Double = 0.0
)

class LabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LabRepository

    init {
        val db = LabDatabase.getInstance(application)
        repository = LabRepository(db.patientDao(), application)
    }

    private val todayFormatted: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Active insertion date (for "+ Naya Patient")
    private val _insertionDate = MutableStateFlow(todayFormatted)
    val insertionDate: StateFlow<String> = _insertionDate.asStateFlow()

    // Date range filter
    private val _filterStartDate = MutableStateFlow("")
    val filterStartDate: StateFlow<String> = _filterStartDate.asStateFlow()

    private val _filterEndDate = MutableStateFlow("")
    val filterEndDate: StateFlow<String> = _filterEndDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val settings: StateFlow<LabSettings> = repository.settingsState

    val allPatients: StateFlow<List<PatientRecord>> = repository.getAllPatientsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered patients
    val filteredPatients: StateFlow<List<PatientRecord>> = combine(
        allPatients,
        _filterStartDate,
        _filterEndDate,
        _searchQuery
    ) { patients, start, end, query ->
        patients.filter { p ->
            val matchStart = start.isBlank() || p.date >= start
            val matchEnd = end.isBlank() || p.date <= end
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.careOf.contains(query, ignoreCase = true)
            matchStart && matchEnd && matchQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary totals
    val summary: StateFlow<LabUiSummary> = filteredPatients.combine(MutableStateFlow(Unit)) { patients, _ ->
        val count = patients.count { it.name.isNotBlank() }
        val bill = patients.sumOf { it.billAmount }
        val less = patients.sumOf { it.lessAmount }
        val paid = patients.sumOf { it.paidAmount }
        val due = maxOf(0.0, bill - less - paid)
        val lessPct = if (bill > 0) (less / bill) * 100.0 else 0.0
        val paidPct = if (bill > 0) (paid / bill) * 100.0 else 0.0

        LabUiSummary(
            totalPatients = count,
            totalBill = bill,
            totalLess = less,
            totalPaid = paid,
            totalDue = due,
            lessPercentage = lessPct,
            paidPercentage = paidPct
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LabUiSummary()
    )

    init {
        // Pre-populate with realistic starter records if empty
        viewModelScope.launch {
            allPatients.collect { list ->
                if (list.isEmpty()) {
                    populateInitialSamples()
                }
            }
        }
    }

    private suspend fun populateInitialSamples() {
        val today = todayFormatted
        val samplePatients = listOf(
            PatientRecord(
                date = today,
                name = "Ramesh Kumar",
                careOf = "Dr. S. K. Gupta",
                billAmount = 850.0,
                lessAmount = 100.0,
                paidAmount = 750.0,
                isManualPaid = false
            ),
            PatientRecord(
                date = today,
                name = "Sunita Sharma",
                careOf = "Dr. Verma Clinic",
                billAmount = 1200.0,
                lessAmount = 200.0,
                paidAmount = 1000.0,
                isManualPaid = false
            ),
            PatientRecord(
                date = today,
                name = "Mohammad Aslam",
                careOf = "City Care Hospital",
                billAmount = 450.0,
                lessAmount = 50.0,
                paidAmount = 400.0,
                isManualPaid = false
            )
        )
        repository.insertAll(samplePatients)
    }

    fun setInsertionDate(date: String) {
        _insertionDate.value = date
    }

    fun setDateFilter(start: String, end: String) {
        _filterStartDate.value = start
        _filterEndDate.value = end
    }

    fun clearDateFilter() {
        _filterStartDate.value = ""
        _filterEndDate.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNewPatient() {
        viewModelScope.launch {
            val date = _insertionDate.value.ifBlank { todayFormatted }
            val newPatient = PatientRecord(
                date = date,
                name = "",
                careOf = "",
                billAmount = 0.0,
                lessAmount = 0.0,
                paidAmount = 0.0,
                isManualPaid = false
            )
            repository.insertPatient(newPatient)
        }
    }

    fun updatePatient(patient: PatientRecord) {
        viewModelScope.launch {
            repository.updatePatient(patient)
        }
    }

    fun updatePatientName(patient: PatientRecord, newName: String) {
        updatePatient(patient.copy(name = newName))
    }

    fun updatePatientCareOf(patient: PatientRecord, newCareOf: String) {
        updatePatient(patient.copy(careOf = newCareOf))
    }

    fun updatePatientBill(patient: PatientRecord, billVal: Double) {
        val newPaid = if (patient.isManualPaid) patient.paidAmount else maxOf(0.0, billVal - patient.lessAmount)
        updatePatient(patient.copy(billAmount = billVal, paidAmount = newPaid))
    }

    fun updatePatientLess(patient: PatientRecord, lessVal: Double) {
        val newPaid = if (patient.isManualPaid) patient.paidAmount else maxOf(0.0, patient.billAmount - lessVal)
        updatePatient(patient.copy(lessAmount = lessVal, paidAmount = newPaid))
    }

    fun updatePatientPaid(patient: PatientRecord, paidVal: Double) {
        // User manually entered paid amount
        updatePatient(patient.copy(paidAmount = paidVal, isManualPaid = true))
    }

    fun resetPatientPaidToAuto(patient: PatientRecord) {
        val autoPaid = maxOf(0.0, patient.billAmount - patient.lessAmount)
        updatePatient(patient.copy(paidAmount = autoPaid, isManualPaid = false))
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            repository.deletePatientById(id)
        }
    }

    fun resetAllList() {
        viewModelScope.launch {
            repository.clearAllPatients()
            repository.clearSignatures()
        }
    }

    fun updatePreparedByName(name: String) {
        val current = settings.value
        repository.updateSettings(current.copy(preparedByName = name))
    }

    fun updatePreparedBySignature(points: String) {
        val current = settings.value
        repository.updateSettings(current.copy(preparedBySignaturePoints = points))
    }

    fun updatePaidByName(name: String) {
        val current = settings.value
        repository.updateSettings(current.copy(paidByName = name))
    }

    fun updatePaidBySignature(points: String) {
        val current = settings.value
        repository.updateSettings(current.copy(paidBySignaturePoints = points))
    }

    fun toggleMarkedPaid(isPaid: Boolean) {
        val current = settings.value
        repository.updateSettings(current.copy(isMarkedPaid = isPaid))
    }
}
