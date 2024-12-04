package com.example.pum

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class AppointmentAdapter(
    private var appointments: MutableList<Appointment>,
    private val onDeleteClick: (Appointment) -> Unit,
    private val onEditClick: (Appointment) -> Unit,
//    private val onHistoryAdd: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    init {
        createNotificationChannel()
    }

    // Helper function to send SMS
    // Helper function to send SMS
    private fun sendSMS(contact: String, appointment: Appointment, context: Context, isUpdate: Boolean) {
        if (contact.isEmpty()) {
            Toast.makeText(context, "Numer telefonu jest pusty!", Toast.LENGTH_SHORT).show()
            return
        }

        val smsManager = SmsManager.getDefault()
        val message = if (isUpdate) {
            "Nowe szczegóły spotkania:\nTytuł: ${appointment.title}\nData: ${appointment.date}\nGodzina: ${appointment.time}"
        } else {
            "Spotkanie zostało odwołane:\nTytuł: ${appointment.title}\nData: ${appointment.date}\nGodzina: ${appointment.time}"
        }

        try {
            // Sending SMS
            smsManager.sendTextMessage(contact, null, message, null, null)
            val toastMessage = if (isUpdate) {
                "Wysłano SMS z nowymi szczegółami do: $contact"
            } else {
                "Wysłano SMS o odwołaniu spotkania do: $contact"
            }
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            Log.d("AppointmentAdapter", "SMS sent to $contact: $message")
        } catch (e: Exception) {
            Log.e("AppointmentAdapter", "Błąd podczas wysyłania SMS", e)
            Toast.makeText(context, "Błąd wysyłania SMS", Toast.LENGTH_SHORT).show()
        }
    }


    // Create Notification Channel
    private fun createNotificationChannel() {
        val name = "Appointment Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("appointment_channel", name, importance).apply {

        }
        val notificationManager: NotificationManager =
            MyApp.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getReminderTime(date: String, time: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.parse("$date $time")?.time ?: System.currentTimeMillis()
    }

    // Share appointment details
    private fun shareAppointmentDetails(appointment: Appointment, context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                """
                Szczegóły spotkania:
                Tytuł: ${appointment.title}
                Data: ${appointment.date}
                Godzina: ${appointment.time}
                """.trimIndent()
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Udostępnij szczegóły spotkania"))
    }

    // Add to calendar
    private fun addToCalendar(appointment: Appointment, context: Context) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, appointment.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, getReminderTime(appointment.date, appointment.time))
        }
        context.startActivity(intent)
    }

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.appointmentTitle)
        private val dateTextView: TextView = view.findViewById(R.id.appointmentDate)
        private val timeTextView: TextView = view.findViewById(R.id.appointmentTime)
        private val contactTextView: TextView = view.findViewById(R.id.contact)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        val editButton: Button = view.findViewById(R.id.editButton)
        val shareButton: Button = view.findViewById(R.id.shareButton)
        val addToCalendarButton: Button = view.findViewById(R.id.addToCalendarButton)

        fun bind(appointment: Appointment) {
            titleTextView.text = appointment.title
            dateTextView.text = appointment.date
            timeTextView.text = appointment.time
            contactTextView.text = appointment.contact
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.bind(appointment)

        // Delete button click
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Usuń spotkanie")
                .setMessage("Czy chcesz usunąć to spotkanie?")
                .setPositiveButton("Usuń") { _, _ ->
                    onDeleteClick(appointment)
                    removeItem(appointment)
                    if (appointment.contact!!.isNotEmpty()) {
                        sendSMS(appointment.contact.toString(), appointment, holder.itemView.context, isUpdate = false)
                    } else {
                        Log.d("AppointmentAdapter", "No contact number to send SMS.")
                    }
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }

        // Edit button click
        holder.editButton.setOnClickListener {
            onEditClick(appointment)
            updateAppointment(appointment, holder.itemView.context)
            if (appointment.contact!!.isNotEmpty()) {
                sendSMS(appointment.contact.toString(), appointment, holder.itemView.context, isUpdate = true)
            } else {
                Log.d("AppointmentAdapter", "No contact number to send SMS.")
            }
        }

        // Share button click
        holder.shareButton.setOnClickListener {
            shareAppointmentDetails(appointment, holder.itemView.context)
        }

        // Add to Calendar button click
        holder.addToCalendarButton.setOnClickListener {
            addToCalendar(appointment, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = appointments.size

    private fun removeItem(appointment: Appointment) {
        val position = appointments.indexOf(appointment)
        if (position != -1) {
            appointments.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Inside your AppointmentAdapter
    private fun updateAppointment(updatedAppointment: Appointment, context: Context) {
        val index = appointments.indexOfFirst { it.id == updatedAppointment.id }
        if (index != -1) {
            appointments[index] = updatedAppointment
            notifyItemChanged(index)
        }
    }


}
