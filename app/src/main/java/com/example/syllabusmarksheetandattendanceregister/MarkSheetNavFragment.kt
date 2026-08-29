package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentMarkSheetNavBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.MarkSheetViewModel

class MarkSheetNavFragment : Fragment() {

    private var _binding: FragmentMarkSheetNavBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarkSheetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarkSheetNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreSelections()
        setupObservers()
        setupListeners()
    }

    private fun restoreSelections() {
        binding.spinnerYear.setText(viewModel.selectedYear.value, false)
        binding.spinnerSubject.setText(viewModel.selectedSubject.value, false)
        binding.spinnerMainClass.setText(viewModel.selectedMainClass.value, false)
        binding.spinnerSubclass.setText(viewModel.selectedSubclass.value, false)
        binding.spinnerSequence.setText(viewModel.selectedSequence.value, false)
        
        binding.layoutMainClass.isEnabled = !viewModel.mainClasses.value.isNullOrEmpty()
        binding.layoutSubclass.isEnabled = !viewModel.subclasses.value.isNullOrEmpty()
    }

    private fun setupObservers() {
        viewModel.years.observe(viewLifecycleOwner) { years ->
            binding.spinnerYear.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years))
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            binding.spinnerSubject.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects))
        }

        viewModel.mainClasses.observe(viewLifecycleOwner) { classes ->
            binding.spinnerMainClass.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, classes))
            binding.layoutMainClass.isEnabled = classes.isNotEmpty()
        }

        viewModel.subclasses.observe(viewLifecycleOwner) { subclasses ->
            binding.spinnerSubclass.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subclasses))
            binding.layoutSubclass.isEnabled = subclasses.isNotEmpty()
        }

        viewModel.sequences.observe(viewLifecycleOwner) { sequences ->
            binding.spinnerSequence.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sequences))
        }

        viewModel.selectedYear.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSubject.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedMainClass.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSubclass.observe(viewLifecycleOwner) { validateForm() }
        viewModel.selectedSequence.observe(viewLifecycleOwner) { validateForm() }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressMarkSheetNav.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !isLoading && viewModel.isFormValid()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        viewModel.navigateToMarkSheetEvent.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.onNavigatedToMarkSheet()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mark_sheet_container, MarkSheetFragment())
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
        binding.spinnerSequence.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectSequence(parent.getItemAtPosition(position) as String)
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.fetchMarkSheet()
        }
    }

    private fun validateForm() {
        binding.btnSubmit.isEnabled = viewModel.isFormValid()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}