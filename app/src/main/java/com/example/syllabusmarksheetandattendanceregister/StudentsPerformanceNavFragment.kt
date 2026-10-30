package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentStudentsPerformanceNavBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsPerformanceViewModel

class StudentsPerformanceNavFragment : Fragment() {

    private var _binding: FragmentStudentsPerformanceNavBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentsPerformanceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsPerformanceNavBinding.inflate(inflater, container, false)
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
        binding.spinnerSequence.setText(viewModel.selectedSequence.value, false)

        binding.layoutMainClass.isEnabled = !viewModel.mainClasses.value.isNullOrEmpty()
    }

    private fun setupObservers() {
        viewModel.years.observe(viewLifecycleOwner) { years ->
            if (years.isNullOrEmpty()) return@observe
            binding.spinnerYear.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years))
            binding.spinnerYear.setText(viewModel.selectedYear.value, false)
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            if (subjects.isNullOrEmpty()) return@observe
            binding.spinnerSubject.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects))
            binding.spinnerSubject.setText(viewModel.selectedSubject.value, false)
        }

        viewModel.mainClasses.observe(viewLifecycleOwner) { classes ->
            binding.spinnerMainClass.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, classes))
            binding.layoutMainClass.isEnabled = classes.isNotEmpty()
            binding.spinnerMainClass.setText(viewModel.selectedMainClass.value, false)
        }

        viewModel.sequences.observe(viewLifecycleOwner) { sequences ->
            binding.spinnerSequence.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sequences))
            binding.spinnerSequence.setText(viewModel.selectedSequence.value, false)
        }

        viewModel.selectedYear.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSubject.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedMainClass.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSequence.observe(viewLifecycleOwner) { validateForm() }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            toggleProgress(isLoading)
            binding.btnSubmit.isEnabled = !isLoading && viewModel.isFormValid()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        viewModel.navigateToPerformanceEvent.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.onNavigatedToPerformance()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.students_performance_container, StudentsPerformanceFragment())
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
        }
        binding.spinnerMainClass.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectMainClass(parent.getItemAtPosition(position) as String)
        }
        binding.spinnerSequence.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectSequence(parent.getItemAtPosition(position) as String)
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.fetchPerformanceStats()
        }
    }

    private fun validateForm() {
        binding.btnSubmit.isEnabled = viewModel.isFormValid()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toggleProgress(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}