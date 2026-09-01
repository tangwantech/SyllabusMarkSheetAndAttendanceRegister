package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.StudentsDatabaseAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.DialogAddStudentBinding
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentStudentsDatabaseBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsDatabaseViewModel

class StudentsDatabaseFragment : Fragment() {

    private var _binding: FragmentStudentsDatabaseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentsDatabaseViewModel by activityViewModels()
    private lateinit var adapter: StudentsDatabaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupHeader()
        setupObservers()
        
        binding.fabAddStudent.setOnClickListener {
            showAddStudentDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentsDatabaseAdapter()
        binding.recyclerStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStudents.adapter = adapter
    }

    private fun setupHeader() {
        val subject = viewModel.selectedSubject.value ?: ""
        val year = viewModel.selectedYear.value ?: ""
        val subclass = viewModel.selectedSubclass.value ?: ""
        
        binding.textHeaderTitle.text = getString(R.string.students_in_subject_format, subject)
        
        viewModel.students.observe(viewLifecycleOwner) { students ->
            val count = students.size
            binding.textHeaderStats.text = getString(R.string.header_students_stats_format, year, subclass, count)
        }
    }

    private fun setupObservers() {
        viewModel.students.observe(viewLifecycleOwner) { students ->
            adapter.updateData(students)
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddStudentDialog() {
        val dialogBinding = DialogAddStudentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val genders = listOf(getString(R.string.gender_m), getString(R.string.gender_f))
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        dialogBinding.spinnerGender.setAdapter(genderAdapter)

        fun validateDialog() {
            val name = dialogBinding.editStudentName.text.toString().trim()
            val matricule = dialogBinding.editMatricule.text.toString().trim()
            val gender = dialogBinding.spinnerGender.text.toString().trim()
            dialogBinding.btnSubmit.isEnabled = name.isNotEmpty() && matricule.isNotEmpty() && gender.isNotEmpty()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateDialog() }
            override fun afterTextChanged(s: Editable?) {}
        }

        dialogBinding.editStudentName.addTextChangedListener(watcher)
        dialogBinding.editMatricule.addTextChangedListener(watcher)
        dialogBinding.spinnerGender.setOnItemClickListener { _, _, _, _ -> validateDialog() }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val name = dialogBinding.editStudentName.text.toString().trim()
            val matricule = dialogBinding.editMatricule.text.toString().trim()
            val gender = dialogBinding.spinnerGender.text.toString().trim()
            
            viewModel.addStudent(name, matricule, gender)
            dialog.dismiss()
            Toast.makeText(requireContext(), R.string.student_added_successfully, Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}