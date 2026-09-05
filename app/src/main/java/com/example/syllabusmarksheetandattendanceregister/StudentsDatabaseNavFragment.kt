package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentStudentsDatabaseNavBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsDatabaseViewModel

class StudentsDatabaseNavFragment : Fragment() {

    private var _binding: FragmentStudentsDatabaseNavBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentsDatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsDatabaseNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupObservers()
        
        binding.btnSubmit.setOnClickListener {
            viewModel.fetchStudents()
        }
    }

    override fun onResume() {
        super.onResume()
        restoreSelections()
    }

    private fun restoreSelections() {
        viewModel.selectedYear.value?.let { binding.spinnerYear.setText(it, false) }
        viewModel.selectedSubject.value?.let { binding.spinnerSubject.setText(it, false) }
        viewModel.selectedMainClass.value?.let { binding.spinnerMainClass.setText(it, false) }
        viewModel.selectedSubclass.value?.let { binding.spinnerSubclass.setText(it, false) }
        validateInput()
    }

    private fun setupSpinners() {
        viewModel.years.observe(viewLifecycleOwner) { years ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
            binding.spinnerYear.setAdapter(adapter)
            binding.spinnerYear.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectYear(years[position])
                validateInput()
            }
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
            binding.spinnerSubject.setAdapter(adapter)
            binding.spinnerSubject.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectSubject(subjects[position])
                binding.spinnerMainClass.setText("", false)
                binding.spinnerSubclass.setText("", false)
                validateInput()
            }
        }

        viewModel.mainClasses.observe(viewLifecycleOwner) { mainClasses ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mainClasses)
            binding.spinnerMainClass.setAdapter(adapter)
            binding.spinnerMainClass.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectMainClass(mainClasses[position])
                binding.spinnerSubclass.setText("", false)
                validateInput()
            }
        }

        viewModel.subclasses.observe(viewLifecycleOwner) { subclasses ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subclasses)
            binding.spinnerSubclass.setAdapter(adapter)
            binding.spinnerSubclass.setOnItemClickListener { _, _, position, _ ->
                viewModel.selectSubclass(subclasses[position])
                validateInput()
            }
        }
    }

    private fun validateInput() {
        val isYearSelected = viewModel.selectedYear.value != null
        val isSubjectSelected = viewModel.selectedSubject.value != null
        val isMainClassSelected = viewModel.selectedMainClass.value != null
        val isSubclassSelected = viewModel.selectedSubclass.value != null

        binding.btnSubmit.isEnabled = isYearSelected && isSubjectSelected && isMainClassSelected && isSubclassSelected
    }

    private fun setupObservers() {
        viewModel.navigateToDatabaseEvent.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.students_database_container, StudentsDatabaseFragment())
                    .addToBackStack(null)
                    .commit()
                viewModel.onNavigatedToDatabase()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}