package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PatientRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patient_records ORDER BY date DESC, id ASC")
    fun getAllPatientsFlow(): Flow<List<PatientRecord>>

    @Query("SELECT * FROM patient_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, id ASC")
    fun getPatientsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<PatientRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientRecord>)

    @Update
    suspend fun updatePatient(patient: PatientRecord)

    @Delete
    suspend fun deletePatient(patient: PatientRecord)

    @Query("DELETE FROM patient_records WHERE id = :id")
    suspend fun deletePatientById(id: Long)

    @Query("DELETE FROM patient_records")
    suspend fun clearAll()
}
