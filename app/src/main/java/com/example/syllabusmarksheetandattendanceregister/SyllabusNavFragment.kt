package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentSyllabusNavBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.SyllabusViewModel

class SyllabusNavFragment : Fragment() {

    private var _binding: FragmentSyllabusNavBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyllabusViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyllabusNavBinding.inflate(inflater, container, false)
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
        
        binding.layoutMainClass.isEnabled = !viewModel.mainClasses.value.isNullOrEmpty()
    }

    private fun setupObservers() {
        viewModel.years.observe(viewLifecycleOwner) { years ->
            if (years.isNullOrEmpty()) return@observe
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
            binding.spinnerYear.setAdapter(adapter)
            binding.spinnerYear.setText(viewModel.selectedYear.value, false)
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            if (subjects.isNullOrEmpty()) return@observe
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
            binding.spinnerSubject.setAdapter(adapter)
            binding.spinnerSubject.setText(viewModel.selectedSubject.value, false)
        }

        viewModel.mainClasses.observe(viewLifecycleOwner) { mainClasses ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mainClasses)
            binding.spinnerMainClass.setAdapter(adapter)
            binding.layoutMainClass.isEnabled = mainClasses.isNotEmpty()
            binding.spinnerMainClass.setText(viewModel.selectedMainClass.value, false)
        }

        viewModel.selectedYear.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSubject.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedMainClass.observe(viewLifecycleOwner) { validateForm() }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressNav.visibility = if (isLoading) View.VISIBLE else View.GONE
            toggleProgress(isLoading)
            binding.btnSubmit.isEnabled = !isLoading && viewModel.isFormValid()
        }

        viewModel.navigateToChaptersEvent.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.onNavigatedToChapters()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SyllabusChaptersFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setupListeners() {
        binding.spinnerYear.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectYear(parent.getItemAtPosition(position) as String)
            viewModel.clearSyllabusFromDatabase(requireContext().applicationContext)
        }

        binding.spinnerSubject.setOnItemClickListener { parent, _, position, _ ->
            val subject = parent.getItemAtPosition(position) as String
            viewModel.selectSubject(subject)
            binding.spinnerMainClass.text = null // Clear UI
            viewModel.clearSyllabusFromDatabase(requireContext().applicationContext)
        }

        binding.spinnerMainClass.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectMainClass(parent.getItemAtPosition(position) as String)
            viewModel.clearSyllabusFromDatabase(requireContext().applicationContext)
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.fetchSyllabusChapters()
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
