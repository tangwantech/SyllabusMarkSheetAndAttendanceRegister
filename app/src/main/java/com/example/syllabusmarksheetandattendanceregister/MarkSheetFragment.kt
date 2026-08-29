package com.example.syllabusmarksheetandattendanceregister

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.StudentsAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.DialogUpdateScoreBinding
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentMarkSheetBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.Student
import com.example.syllabusmarksheetandattendanceregister.repositories.MarkSheetRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.MarkSheetViewModel

class MarkSheetFragment : Fragment(), StudentsAdapter.ItemClickLister, StudentsAdapter.SwitchStateChangeListener{

    private var _binding: FragmentMarkSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarkSheetViewModel by activityViewModels()
    private lateinit var adapter: StudentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarkSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = StudentsAdapter(this, this)
        binding.recyclerStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStudents.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.markSheetData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.textHeaderSubject.text = getString(R.string.marksheet_header_subject_format, viewModel.selectedSubject.value)
                binding.textHeaderSubclass.text = getString(R.string.marksheet_header_subclass_format, viewModel.selectedSubclass.value)
                binding.textHeaderSequence.text = viewModel.selectedSequence.value
                
                val enrolledCount = data.students.count { it.isRegistered }
                val passedCount = data.students.count { it.isRegistered && it.score in 10.0..20.0 }
                val percentage = if (enrolledCount > 0) (passedCount.toDouble() / enrolledCount) * 100.0 else 0.0
                binding.textHeaderOverallStats.text = getString(R.string.marksheet_stats_combined_format, enrolledCount, passedCount, percentage)

                // Male Stats
                val enrolledMales = data.students.count { it.isRegistered && (it.gender.equals("M", ignoreCase = true) || it.gender.equals("Male", ignoreCase = true)) }
                val passedMales = data.students.count { it.isRegistered && (it.gender.equals("M", ignoreCase = true) || it.gender.equals("Male", ignoreCase = true)) && it.score in 10.0..20.0 }
                val malePercentage = if (enrolledMales > 0) (passedMales.toDouble() / enrolledMales) * 100.0 else 0.0
                binding.textHeaderMalesCombined.text = getString(R.string.males_stats_combined_format, enrolledMales, passedMales, malePercentage)

                // Female Stats
                val enrolledFemales = data.students.count { it.isRegistered && (it.gender.equals("F", ignoreCase = true) || it.gender.equals("Female", ignoreCase = true)) }
                val passedFemales = data.students.count { it.isRegistered && (it.gender.equals("F", ignoreCase = true) || it.gender.equals("Female", ignoreCase = true)) && it.score in 10.0..20.0 }
                val femalePercentage = if (enrolledFemales > 0) (passedFemales.toDouble() / enrolledFemales) * 100.0 else 0.0
                binding.textHeaderFemalesCombined.text = getString(R.string.females_stats_combined_format, enrolledFemales, passedFemales, femalePercentage)

                adapter.updateData(data.students)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressMarkSheet.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveMarkSheet.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.btnSaveMarkSheet.setOnClickListener {
            viewModel.saveMarkSheet(object : MarkSheetRepository.UpdateMarkSheetListener {
                override fun onUpdateSuccessful(result: String) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), getString(R.string.update_successful), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: String) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun showUpdateScoreDialog(initialIndex: Int, student: Student) {
        val dialogBinding = DialogUpdateScoreBinding.inflate(layoutInflater)
        var currentIndex = initialIndex
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        fun updateDialogUI(index: Int) {
            val currentStudent = viewModel.markSheetData.value?.students?.getOrNull(index) ?: return
            dialogBinding.textStudentNameTitle.text = currentStudent.name
            dialogBinding.editScore.setText(currentStudent.score.toString())
            dialogBinding.editScore.requestFocus()
            dialogBinding.editScore.selectAll()
            
            val students = viewModel.markSheetData.value?.students ?: emptyList()
            dialogBinding.btnPrevious.isEnabled = (0 until index).any { students[it].isRegistered }
            dialogBinding.btnNext.isEnabled = (index + 1 until students.size).any { students[it].isRegistered }
        }

        fun saveCurrentScore(): Boolean {
            val scoreStr = dialogBinding.editScore.text.toString()
            val score = scoreStr.toDoubleOrNull()
            return if (score != null && score in 0.0..20.0) {
                dialogBinding.textInputLayoutScore.error = null
                viewModel.updateStudentScore(currentIndex, score)
                true
            } else {
                dialogBinding.textInputLayoutScore.error = getString(R.string.error_invalid_score)
                false
            }
        }

        dialogBinding.btnPrevious.setOnClickListener {
            if (saveCurrentScore()) {
                val students = viewModel.markSheetData.value?.students ?: return@setOnClickListener
                var nextIndex = currentIndex - 1
                while (nextIndex >= 0 && !students[nextIndex].isRegistered) {
                    nextIndex--
                }
                if (nextIndex >= 0) {
                    currentIndex = nextIndex
                    updateDialogUI(currentIndex)
                }
            }
        }

        dialogBinding.btnNext.setOnClickListener {
            if (saveCurrentScore()) {
                val students = viewModel.markSheetData.value?.students ?: return@setOnClickListener
                var nextIndex = currentIndex + 1
                while (nextIndex < students.size && !students[nextIndex].isRegistered) {
                    nextIndex++
                }
                if (nextIndex < students.size) {
                    currentIndex = nextIndex
                    updateDialogUI(currentIndex)
                }
            }
        }

        dialogBinding.btnFinish.setOnClickListener {
            if (saveCurrentScore()) {
                dialog.dismiss()
            }
        }

        dialogBinding.editScore.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val score = s.toString().toDoubleOrNull()
                if (score != null && score in 0.0..20.0) {
                    dialogBinding.textInputLayoutScore.error = null
                } else if (s.isNullOrEmpty()) {
                    dialogBinding.textInputLayoutScore.error = null
                } else {
                    dialogBinding.textInputLayoutScore.error = getString(R.string.error_invalid_score)
                }
            }
        })

        updateDialogUI(currentIndex)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int, student: Student) {
        if(student.isRegistered){
            showUpdateScoreDialog(position, student)
        }

    }

    override fun onSwitchStateChange(studentIndex: Int, state: Boolean) {
        viewModel.updateStudentRegistration(studentIndex, state)
    }
}