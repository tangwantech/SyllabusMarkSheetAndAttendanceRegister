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
import com.example.syllabusmarksheetandattendanceregister.datamodels.MarkSheetData
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

                // Male Stats
                val enrolledMales = data.students.count { it.isRegistered && (it.gender.equals("M", ignoreCase = true) || it.gender.equals("Male", ignoreCase = true)) }
                val passedMales = data.students.count { it.isRegistered && (it.gender.equals("M", ignoreCase = true) || it.gender.equals("Male", ignoreCase = true)) && it.score in 10.0..20.0 }
                val malePercentage = if (enrolledMales > 0) (passedMales.toDouble() / enrolledMales) * 100.0 else 0.0

                // Female Stats
                val enrolledFemales = data.students.count { it.isRegistered && (it.gender.equals("F", ignoreCase = true) || it.gender.equals("Female", ignoreCase = true)) }
                val passedFemales = data.students.count { it.isRegistered && (it.gender.equals("F", ignoreCase = true) || it.gender.equals("Female", ignoreCase = true)) && it.score in 10.0..20.0 }
                val femalePercentage = if (enrolledFemales > 0) (passedFemales.toDouble() / enrolledFemales) * 100.0 else 0.0

                // Update Table
                binding.textMaleEnrolled.text = enrolledMales.toString()
                binding.textMalePassed.text = passedMales.toString()
                binding.textMalePercentage.text = getString(R.string.percentage_decimal_format, malePercentage)

                binding.textFemaleEnrolled.text = enrolledFemales.toString()
                binding.textFemalePassed.text = passedFemales.toString()
                binding.textFemalePercentage.text = getString(R.string.percentage_decimal_format, femalePercentage)

                binding.textTotalEnrolled.text = enrolledCount.toString()
                binding.textTotalPassed.text = passedCount.toString()
                binding.textTotalPercentage.text = getString(R.string.percentage_decimal_format, percentage)

                adapter.updateData(data.students)

//                binding.noDataTextView.visibility = View.GONE
//                binding.btnSaveMarkSheet.isEnabled = true
            }

            val isEmpty = data?.students.isNullOrEmpty()
            binding.noDataTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.btnSaveMarkSheet.isEnabled = !isEmpty && (viewModel.isLoading.value == false)
//            binding.btnSaveMarkSheet.isEnabled = changeSaveButtonState(data)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            toggleProgress(isLoading)
            binding.btnSaveMarkSheet.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.btnSaveMarkSheet.setOnClickListener {

            viewModel.saveMarkSheet(object : MarkSheetRepository.UpdateMarkSheetListener {
                override fun onUpdateSuccessful(result: String) {
                    activity?.runOnUiThread {
                        updateIsDataSaved(true)
                        Toast.makeText(requireContext(), getString(R.string.update_successful), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: String) {
                    activity?.runOnUiThread {
                        updateIsDataSaved(false)
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
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
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

    private fun toggleProgress(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun updateIsDataSaved(state:Boolean){
        viewModel.updateIsDataSave(state)
    }

//    fun changeSaveButtonState (data: MarkSheetData?): Boolean{
//        return data != null
//    }


}