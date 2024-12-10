package com.example.pum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pum.databinding.FragmentSecondBinding

@Suppress("DEPRECATION")
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val appointmentViewModel: AppointmentViewModel by activityViewModels()

    // Variable to store the selected date
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the CalendarView listener to store the selected date
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Format the selected date
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

        }

        // Navigate to the first fragment when the button is clicked
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        // Navigate to the third fragment for viewing appointments
        binding.viewAppointmentsButton.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }

        // Use the "View Appointments for the Day" button to navigate with the selected date
        binding.viewAppointmentsForDayButton.setOnClickListener {
            // Ensure that a date has been selected
            selectedDate?.let { date ->
                val action = SecondFragmentDirections.actionSecondFragmentToViewDayFragment(date)
                findNavController().navigate(action)
            } ?: run {
                // Show a toast if no date is selected
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
