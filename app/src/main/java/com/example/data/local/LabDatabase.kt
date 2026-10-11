package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.PatientRecord

@Database(entities = [PatientRecord::class], version = 1, exportSchema = false)
abstract class LabDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao

    companion object {
        @Volatile
        private var INSTANCE: LabDatabase? = null

        fun getInstance(context: Context): LabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LabDatabase::class.java,
                    "sharda_lab_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
