package com.example.pum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppointmentAdapter(
    private var appointments: MutableList<Appointment>,
    private val onDeleteClick: (Appointment) -> Unit,
    private val onEditClick: (Appointment) -> Unit,
    private val onShareClick: (Appointment) -> Unit, // Add this callback
    private val onAddToCalendarClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    // Metoda do inflacji widoku i tworzenia ViewHoldera
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    // Metoda do wiązania danych z widokiem
    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        holder.appointmentTitle.text = appointment.title
        holder.appointmentDate.text = appointment.date
        holder.appointmentTime.text = appointment.time
        holder.contact.text = appointment.contact

        // Obsługuje przyciski edycji i usuwania
        holder.deleteButton.setOnClickListener {
            onDeleteClick(appointment)
        }

        holder.editButton.setOnClickListener {
            onEditClick(appointment)
        }
        holder.shareButton.setOnClickListener {
            onShareClick(appointment) }
        holder.addToCalendarButton.setOnClickListener {
            onAddToCalendarClick(appointment) }
    }

    // Zwraca liczbę spotkań
    override fun getItemCount(): Int {
        return appointments.size
    }

    // Aktualizacja listy spotkań
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments.clear()
        appointments.addAll(newAppointments)
        notifyDataSetChanged()
    }

    // ViewHolder dla pojedynczego spotkania
    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appointmentTitle: TextView = view.findViewById(R.id.appointmentTitle)
        val appointmentDate: TextView = view.findViewById(R.id.appointmentDate)
        val appointmentTime: TextView = view.findViewById(R.id.appointmentTime)
        val contact: TextView = view.findViewById(R.id.contact)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        val editButton: Button = view.findViewById(R.id.editButton)
        val shareButton: Button = view.findViewById(R.id.shareButton)
        val addToCalendarButton: Button = view.findViewById(R.id.addToCalendarButton)
    }
}
