package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.PatientDao
import com.example.data.model.LabSettings
import com.example.data.model.PatientRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LabRepository(
    private val patientDao: PatientDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sharda_lab_prefs", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState = _settingsState.asStateFlow()

    fun getAllPatientsFlow(): Flow<List<PatientRecord>> = patientDao.getAllPatientsFlow()

    suspend fun insertPatient(patient: PatientRecord): Long = patientDao.insertPatient(patient)

    suspend fun updatePatient(patient: PatientRecord) = patientDao.updatePatient(patient)

    suspend fun deletePatientById(id: Long) = patientDao.deletePatientById(id)

    suspend fun clearAllPatients() = patientDao.clearAll()

    suspend fun insertAll(patients: List<PatientRecord>) = patientDao.insertAll(patients)

    private fun loadSettings(): LabSettings {
        return LabSettings(
            preparedByName = prefs.getString("prepared_by_name", "") ?: "",
            preparedBySignaturePoints = prefs.getString("prepared_by_sig", "") ?: "",
            paidByName = prefs.getString("paid_by_name", "") ?: "",
            paidBySignaturePoints = prefs.getString("paid_by_sig", "") ?: "",
            isMarkedPaid = prefs.getBoolean("is_marked_paid", true)
        )
    }

    fun updateSettings(settings: LabSettings) {
        prefs.edit()
            .putString("prepared_by_name", settings.preparedByName)
            .putString("prepared_by_sig", settings.preparedBySignaturePoints)
            .putString("paid_by_name", settings.paidByName)
            .putString("paid_by_sig", settings.paidBySignaturePoints)
            .putBoolean("is_marked_paid", settings.isMarkedPaid)
            .apply()
        _settingsState.value = settings
    }

    fun clearSignatures() {
        val current = _settingsState.value
        val updated = current.copy(
            preparedByName = "",
            preparedBySignaturePoints = "",
            paidByName = "",
            paidBySignaturePoints = ""
        )
        updateSettings(updated)
    }
}
