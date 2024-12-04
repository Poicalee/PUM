package com.example.pum

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment)

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("SELECT * FROM appointments")
    fun getAllAppointments(): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date = :date")
    fun getAppointmentsByDate(date: String): LiveData<List<Appointment>>
}


