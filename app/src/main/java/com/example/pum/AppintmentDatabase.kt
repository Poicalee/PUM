package com.example.pum

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Appointment::class, HistoryAppointment::class], version = 2, exportSchema = false)
abstract class AppointmentDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun historyAppointmentDao(): HistoryAppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppointmentDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Define specific migration steps if schema changed
                // For example: database.execSQL("ALTER TABLE appointments ADD COLUMN contact TEXT")
            }
        }

        fun getDatabase(context: Context): AppointmentDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppointmentDatabase::class.java,
                    "appointment_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
        }
    }
}
