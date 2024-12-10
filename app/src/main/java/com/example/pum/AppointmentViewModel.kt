package com.example.pum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppointmentRepository
    val allAppointments: LiveData<List<Appointment>>

    init {
        val appointmentDao = AppointmentDatabase.getDatabase(application).appointmentDao()
        repository = AppointmentRepository(appointmentDao)
        allAppointments = repository.getAllAppointments()
    }

    // Function to add an appointment
    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.addAppointment(appointment)
        }
    }

    // Function to update an appointment
    fun updateAppointment(updatedAppointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(updatedAppointment)
        }
    }

    // Function to delete an appointment
    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.delete(appointment)
        }
    }

    // Function to filter appointments by date
    fun filterAppointmentsByDate(date: String): LiveData<List<Appointment>> {
        return repository.getAppointmentsByDate(date)
    }
}

