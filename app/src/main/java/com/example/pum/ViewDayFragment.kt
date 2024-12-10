package com.example.pum

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pum.databinding.FragmentViewDayBinding
import java.util.Calendar

class ViewDayFragment : Fragment() {

    private lateinit var contactInput: EditText
    private var _binding: FragmentViewDayBinding? = null
    private val binding get() = _binding!!

    private lateinit var appointmentAdapter: AppointmentAdapter
    private val appointmentViewModel: AppointmentViewModel by activityViewModels()

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val contactUri: Uri? = data?.data
            val cursor = contactUri?.let { requireContext().contentResolver.query(it, null, null, null, null) }

            cursor?.apply {
                if (moveToFirst()) {
                    val nameIndex = getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val contactName = getString(nameIndex)

                    contactInput.setText(contactName) // Set the selected contact
                }
                close()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the selected date passed from Safe Args or the Bundle
        val selectedDate = arguments?.getString("selectedDate") ?: return

        // Update the date text view to show the selected date
        binding.textViewSelectedDate.text = "Appointments for $selectedDate"

        // Set up the RecyclerView
        appointmentAdapter = AppointmentAdapter(
            appointments = mutableListOf(),
            onDeleteClick = { appointment ->
                // Handle the delete action here
                appointmentViewModel.deleteAppointment(appointment)
            },
            onEditClick = { appointment ->
                // Handle the edit action here
                showEditAppointmentDialog(appointment)
            },
            onShareClick = { appointment ->
                // Handle the share action here
                shareAppointment(appointment)
            },
            onAddToCalendarClick = { appointment ->
                // Handle the add to calendar action
                addToCalendar(appointment)
            }
        )

        binding.recyclerViewDayAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
        }

        // Observe and filter the appointments based on the selected date
        appointmentViewModel.filterAppointmentsByDate(selectedDate).observe(viewLifecycleOwner) { appointments ->
            appointmentAdapter.updateAppointments(appointments)

            // Show/hide the message when there are no appointments for the selected date
            binding.textViewNoAppointments.visibility =
                if (appointments.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.addAppointmentButton.setOnClickListener {
            showAddAppointmentDialog()  // Show the dialog to add a new appointment
        }

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_ViewDayFragment_to_SecondFragment)
        }
    }

    private fun shareAppointment(appointment: Appointment) {
        val message = """
            Appointment Details:
            Title: ${appointment.title}
            Date: ${appointment.date}
            Time: ${appointment.time}
            Contact: ${appointment.contact}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Share Appointment"))
    }

    private fun addToCalendar(appointment: Appointment) {
        val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, appointment.title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.contact)
            putExtra(CalendarContract.Events.DESCRIPTION, "Appointment with ${appointment.contact}")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, getAppointmentStartTime(appointment))

        }

        startActivity(calendarIntent)
    }

    private fun getAppointmentStartTime(appointment: Appointment): Long {
        // Parse the date and time strings into a Calendar object
        val calendar = Calendar.getInstance()
        val dateParts = appointment.date.split("-")
        val timeParts = appointment.time.split(":")
        calendar.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt(),
            timeParts[0].toInt(), timeParts[1].toInt())
        return calendar.timeInMillis
    }

    private fun showAddAppointmentDialog() {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val titleInput = EditText(requireContext()).apply {
            hint = "Wprowadź tytuł"
        }
        val dateInput = EditText(requireContext()).apply {
            hint = "Wybierz datę (YYYY-MM-DD)"
            isFocusable = false
            setOnClickListener { showDatePicker(this) }
        }
        val timeInput = EditText(requireContext()).apply {
            hint = "Wybierz godzinę (HH:mm)"
            isFocusable = false
            setOnClickListener { showTimePicker(this) }
        }

        // Contact as an EditText, which is non-editable, but clicking on it opens the contact picker
        contactInput = EditText(requireContext()).apply {
            hint = "Wybierz kontakt"
            isFocusable = false
            setOnClickListener { pickContact() } // Launch the contact picker
        }

        dialogLayout.addView(titleInput)
        dialogLayout.addView(dateInput)
        dialogLayout.addView(timeInput)
        dialogLayout.addView(contactInput)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Dodaj spotkanie")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val title = titleInput.text.toString()
                val date = dateInput.text.toString()
                val time = timeInput.text.toString()
                val contact = contactInput.text.toString()

                if (title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && contact.isNotBlank()) {
                    val newAppointment = Appointment(title = title, date = date, time = time, contact = contact)
                    appointmentViewModel.addAppointment(newAppointment)
                    Toast.makeText(requireContext(), "Dodano spotkanie", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Wszystkie pola są wymagane", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()
    }

    private fun showEditAppointmentDialog(appointment: Appointment) {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val titleInput = EditText(requireContext()).apply {
            hint = "Wprowadź tytuł"
            setText(appointment.title)
        }
        val dateInput = EditText(requireContext()).apply {
            hint = "Wybierz datę (YYYY-MM-DD)"
            setText(appointment.date)
            isFocusable = false
            setOnClickListener { showDatePicker(this) }
        }
        val timeInput = EditText(requireContext()).apply {
            hint = "Wybierz godzinę (HH:mm)"
            setText(appointment.time)
            isFocusable = false
            setOnClickListener { showTimePicker(this) }
        }

        contactInput = EditText(requireContext()).apply {
            hint = "Wybierz kontakt"
            setText(appointment.contact)
            isFocusable = false
            setOnClickListener { pickContact() }
        }

        dialogLayout.addView(titleInput)
        dialogLayout.addView(dateInput)
        dialogLayout.addView(timeInput)
        dialogLayout.addView(contactInput)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edytuj spotkanie")
            .setView(dialogLayout)
            .setPositiveButton("Zapisz") { _, _ ->
                val title = titleInput.text.toString()
                val date = dateInput.text.toString()
                val time = timeInput.text.toString()
                val contact = contactInput.text.toString()

                if (title.isNotBlank() && date.isNotBlank() && time.isNotBlank() && contact.isNotBlank()) {
                    val updatedAppointment = Appointment(id = appointment.id, title = title, date = date, time = time, contact = contact)
                    appointmentViewModel.updateAppointment(updatedAppointment)
                    Toast.makeText(requireContext(), "Spotkanie zaktualizowane", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Wszystkie pola są wymagane", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()
    }

    // Function to pick a contact
    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    @SuppressLint("DefaultLocale")
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                editText.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                editText.setText(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

