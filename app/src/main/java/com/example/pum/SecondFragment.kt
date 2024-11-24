package com.example.pum

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.DatePicker
import android.widget.TimePicker
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.navigation.fragment.findNavController
import com.example.pum.databinding.FragmentSecondBinding
import java.util.*

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    // ID kanału powiadomień
    private val CHANNEL_ID = "reminder_channel"

    // Zmienna do przechowywania daty i godziny przypomnienia
    private var reminderCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sprawdzenie uprawnień do powiadomień
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // Inicjalizacja kanału powiadomień
        createNotificationChannel()

        // Przycisk do nawigacji do FirstFragment
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        // Przycisk do wyboru kontaktu
        binding.selectContactButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, CONTACT_PICKER_REQUEST_CODE)
        }

        // Przycisk do ustawiania przypomnienia
        binding.setReminderButton.setOnClickListener {
            // Otwarcie dialogu wyboru daty
            openDatePickerDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val CONTACT_PICKER_REQUEST_CODE = 1
    }

    // Metoda do odbierania wybranego kontaktu
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data
            // Możesz przetwarzać dane kontaktu, np. wyświetlić wybrany kontakt
        }
    }

    // Funkcja do tworzenia kanału powiadomień (wymagane od Androida 8.0)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Channel"
            val descriptionText = "Channel for reminder notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Rejestracja kanału w systemie
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Funkcja do wysyłania powiadomienia
    @SuppressLint("NotificationPermission")
    private fun sendReminderNotification() {
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(requireContext(), SecondFragment::class.java) // Możesz ustawić odpowiednią akcję

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info) // Ikona powiadomienia
            .setContentTitle("Przypomnienie")
            .setContentText("Masz zaplanowaną wizytę! Sprawdź szczegóły.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)  // Powiadomienie zniknie po kliknięciu
            .setContentIntent(pendingIntent)  // Dodajemy akcję przy kliknięciu
            .build()

        // Wyślij powiadomienie
        notificationManager.notify(0, notification)
    }

    // Funkcja do otwierania dialogu wyboru daty
    private fun openDatePickerDialog() {
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            android.app.DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                // Ustawienie wybranej daty
                reminderCalendar.set(Calendar.YEAR, year)
                reminderCalendar.set(Calendar.MONTH, month)
                reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Po wybraniu daty otwórz dialog wyboru godziny
                openTimePickerDialog()
            },
            reminderCalendar.get(Calendar.YEAR),
            reminderCalendar.get(Calendar.MONTH),
            reminderCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Funkcja do otwierania dialogu wyboru godziny
    private fun openTimePickerDialog() {
        val timePickerDialog = android.app.TimePickerDialog(
            requireContext(),
            android.app.TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                // Ustawienie wybranej godziny
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                reminderCalendar.set(Calendar.MINUTE, minute)

                // Po wybraniu godziny ustaw powiadomienie
                scheduleReminderNotification()
            },
            reminderCalendar.get(Calendar.HOUR_OF_DAY),
            reminderCalendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    // Funkcja do zaplanowania powiadomienia na wybraną datę i godzinę
    private fun scheduleReminderNotification() {
        val notificationManager: NotificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(requireContext(), SecondFragment::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Przypomnienie")
            .setContentText("Masz zaplanowaną wizytę! Sprawdź szczegóły.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Ustawienie powiadomienia na wybraną datę i godzinę
        val triggerTime = reminderCalendar.timeInMillis
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.setExact(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
    
}
