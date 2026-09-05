package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentAttendanceNavBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.AttendanceRegisterViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AttendanceNavFragment : Fragment() {

    private var _binding: FragmentAttendanceNavBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceRegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadInitialData()
        restoreSelections()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        restoreSelections()
    }

    private fun restoreSelections() {
        binding.spinnerYear.setText(viewModel.selectedYear.value, false)
        binding.spinnerSubject.setText(viewModel.selectedSubject.value, false)
        binding.spinnerMainClass.setText(viewModel.selectedMainClass.value, false)
        binding.spinnerSubclass.setText(viewModel.selectedSubclass.value, false)
        binding.editDate.setText(viewModel.selectedDate.value)
        binding.spinnerAbsenceWeight.setText(viewModel.selectedAbsenceWeight.value, false)

        binding.layoutMainClass.isEnabled = !viewModel.mainClasses.value.isNullOrEmpty()
        binding.layoutSubclass.isEnabled = !viewModel.subclasses.value.isNullOrEmpty()
    }

    private fun setupObservers() {
        viewModel.years.observe(viewLifecycleOwner) { years ->
            if (years.isNullOrEmpty()) return@observe
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
            binding.spinnerYear.setAdapter(adapter)
            // If already have a selection, ensure it's shown correctly after adapter is set
            binding.spinnerYear.setText(viewModel.selectedYear.value, false)
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            if (subjects.isNullOrEmpty()) return@observe
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
            binding.spinnerSubject.setAdapter(adapter)
            binding.spinnerSubject.setText(viewModel.selectedSubject.value, false)
        }

        viewModel.mainClasses.observe(viewLifecycleOwner) { classes ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, classes)
            binding.spinnerMainClass.setAdapter(adapter)
            binding.layoutMainClass.isEnabled = classes.isNotEmpty()
            binding.spinnerMainClass.setText(viewModel.selectedMainClass.value, false)
        }

        viewModel.subclasses.observe(viewLifecycleOwner) { subclasses ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subclasses)
            binding.spinnerSubclass.setAdapter(adapter)
            binding.layoutSubclass.isEnabled = subclasses.isNotEmpty()
            binding.spinnerSubclass.setText(viewModel.selectedSubclass.value, false)
        }

        // Validate form when selections change
        viewModel.selectedYear.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSubject.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedMainClass.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSubclass.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedDate.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedAbsenceWeight.observe(viewLifecycleOwner) { validateForm() }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        viewModel.navigateToRegisterEvent.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.onNavigatedToRegister()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.attendance_container, AttendanceRegisterFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setupListeners() {
        binding.spinnerYear.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectYear(parent.getItemAtPosition(position) as String)
        }

        binding.spinnerSubject.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectSubject(parent.getItemAtPosition(position) as String)
            binding.spinnerMainClass.text = null
            binding.spinnerSubclass.text = null
        }

        binding.spinnerMainClass.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectMainClass(parent.getItemAtPosition(position) as String)
            binding.spinnerSubclass.text = null
        }

        binding.spinnerSubclass.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectSubclass(parent.getItemAtPosition(position) as String)
        }

        val absenceWeights = (1..2).map { it.toString() }
        binding.spinnerAbsenceWeight.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, absenceWeights))
        binding.spinnerAbsenceWeight.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectAbsenceWeight(parent.getItemAtPosition(position) as String)
        }

        binding.editDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.fetchAttendanceRegister()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val formattedDate = sdf.format(Date(selection))
            binding.editDate.setText(formattedDate)
            viewModel.selectDate(formattedDate)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun validateForm() {
        binding.btnSubmit.isEnabled = 
            !viewModel.selectedYear.value.isNullOrEmpty() &&
            !viewModel.selectedSubject.value.isNullOrEmpty() &&
            !viewModel.selectedMainClass.value.isNullOrEmpty() &&
            !viewModel.selectedSubclass.value.isNullOrEmpty() &&
            !viewModel.selectedDate.value.isNullOrEmpty() &&
            !viewModel.selectedAbsenceWeight.value.isNullOrEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}