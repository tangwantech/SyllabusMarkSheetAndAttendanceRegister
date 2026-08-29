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
                binding.textHeaderEnrolled.text = getString(R.string.enrolled_count_format, enrolledCount)

                val passedCount = data.students.count { it.isRegistered && it.score in 10.0..20.0 }
                binding.textHeaderPassed.text = getString(R.string.passed_count_format, passedCount)

                if (enrolledCount > 0) {
                    val percentage = (passedCount.toDouble() / enrolledCount.toDouble()) * 100.0
                    binding.textHeaderPassPercentage.text = getString(R.string.passed_percentage_format, percentage)
                    binding.textHeaderPassPercentage.visibility = View.VISIBLE
                } else {
                    binding.textHeaderPassPercentage.visibility = View.GONE
                }

                // Male Stats
                val enrolledMales = data.students.count { it.isRegistered && (it.gender.equals("M", ignoreCase = true) || it.gender.equals("Male", ignoreCase = true)) }
                val passedMales = data.students.count { it.isRegistered && (it.gender.equals("M", ignoreCase = true) || it.gender.equals("Male", ignoreCase = true)) && it.score in 10.0..20.0 }
                
                binding.textHeaderMales.text = getString(R.string.males_label, enrolledMales)
                binding.textHeaderMalesPassed.text = getString(R.string.passed_count_format, passedMales)
                
                if (enrolledMales > 0) {
                    val malePercentage = (passedMales.toDouble() / enrolledMales) * 100.0
                    binding.textHeaderMalesPassPercentage.text = getString(R.string.passed_percentage_format, malePercentage)
                    binding.textHeaderMalesPassPercentage.visibility = View.VISIBLE
                } else {
                    binding.textHeaderMalesPassPercentage.visibility = View.GONE
                }

                // Female Stats
                val enrolledFemales = data.students.count { it.isRegistered && (it.gender.equals("F", ignoreCase = true) || it.gender.equals("Female", ignoreCase = true)) }
                val passedFemales = data.students.count { it.isRegistered && (it.gender.equals("F", ignoreCase = true) || it.gender.equals("Female", ignoreCase = true)) && it.score in 10.0..20.0 }
                
                binding.textHeaderFemales.text = getString(R.string.females_label, enrolledFemales)
                binding.textHeaderFemalesPassed.text = getString(R.string.passed_count_format, passedFemales)

                if (enrolledFemales > 0) {
                    val femalePercentage = (passedFemales.toDouble() / enrolledFemales) * 100.0
                    binding.textHeaderFemalesPassPercentage.text = getString(R.string.passed_percentage_format, femalePercentage)
                    binding.textHeaderFemalesPassPercentage.visibility = View.VISIBLE
                } else {
                    binding.textHeaderFemalesPassPercentage.visibility = View.GONE
                }

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
            
            val students = viewModel.markSheetData.value?.students ?: emptyList()
            dialogBinding.btnPrevious.isEnabled = (0 until index).any { students[it].isRegistered }
            dialogBinding.btnNext.isEnabled = (index + 1 until students.size).any { students[it].isRegistered }
        }

        fun saveCurrentScore(): Boolean {
            val scoreStr = dialogBinding.editScore.text.toString()
            val score = scoreStr.toDoubleOrNull()
            return if (score != null && score in 0.0..20.0) {
                viewModel.updateStudentScore(currentIndex, score)
                true
            } else {
                Toast.makeText(requireContext(), R.string.error_invalid_score, Toast.LENGTH_SHORT).show()
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