package com.example.pum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppointmentRepository
    val allAppointments: LiveData<List<Appointment>>
    private val _appointments = MutableLiveData<MutableList<Appointment>>()
    private val database = AppointmentDatabase.getDatabase(application)
    private val historyDao = database.historyAppointmentDao()

//    val historyAppointments: LiveData<List<HistoryAppointment>> =
//        historyDao.getAllHistoryAppointments().asLiveData()
    init {
        val appointmentDao = AppointmentDatabase.getDatabase(application).appointmentDao()
        repository = AppointmentRepository(appointmentDao)
        allAppointments = repository.getAllAppointments()
        _appointments.value = mutableListOf()  // Inicjalizujemy pustą listę
    }

    // Funkcja do dodawania spotkania
    fun addAppointment(appointment: Appointment) {
        // Add the appointment to the database via the repository
        viewModelScope.launch {
            repository.addAppointment(appointment)
        }
    }

    fun updateAppointment(updatedAppointment: Appointment) {
        viewModelScope.launch {
            // Call the method from the repository
            repository.updateAppointment(updatedAppointment)
        }
    }


    // Metoda do usuwania spotkania
    fun deleteAppointment(appointment: Appointment) {
       viewModelScope.launch {
           repository.delete(appointment)
       }
    }
    fun addToHistory(historyAppointment: HistoryAppointment) = viewModelScope.launch {
        historyDao.insert(historyAppointment)
    }
}
