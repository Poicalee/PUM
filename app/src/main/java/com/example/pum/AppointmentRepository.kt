package com.example.pum

import androidx.lifecycle.LiveData

class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    // Funkcja do pobierania wszystkich spotkań
    fun getAllAppointments(): LiveData<List<Appointment>> {
        return appointmentDao.getAllAppointments()
    }

    // Funkcja do pobierania spotkań po dacie
    fun getAppointmentsByDate(date: String): LiveData<List<Appointment>> {
        return appointmentDao.getAppointmentsByDate(date)
    }

    // Funkcja do dodawania spotkania
    suspend fun addAppointment(appointment: Appointment) {
        appointmentDao.insert(appointment)
    }

    // Funkcja do usuwania spotkania
    suspend fun delete(appointment: Appointment) {
        appointmentDao.delete(appointment)
    }

    // Funkcja do aktualizowania spotkania
    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.update(appointment)
    }
}

