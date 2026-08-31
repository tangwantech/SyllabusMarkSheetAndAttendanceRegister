package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.AttendanceRecordsAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentStudentAttendanceDetailsBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.AttendanceRegisterViewModel

class StudentAttendanceDetailsFragment : Fragment() {

    private var _binding: FragmentStudentAttendanceDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceRegisterViewModel by activityViewModels()
    private lateinit var adapter: AttendanceRecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentAttendanceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = AttendanceRecordsAdapter()
        binding.recyclerAttendanceRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAttendanceRecords.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.selectedStudent.observe(viewLifecycleOwner) { student ->
            if (student != null) {
                updateUI(student)
                adapter.updateRecords(student.attendances)
            }
        }
    }

    private fun updateUI(student: com.example.syllabusmarksheetandattendanceregister.datamodels.StudentAttendanceData) {
        binding.textHeaderStudentName.text = student.name
        binding.textHeaderMatriculeGender.text = getString(R.string.student_matricule_gender_format, student.matricule, student.gender)
        binding.textHeaderSubjectClass.text = getString(R.string.student_subject_class_format, viewModel.selectedSubject.value, viewModel.selectedMainClass.value)
        
        val totalAbsences = student.attendances?.values?.filter { it.isPresent == false }?.sumOf { it.absenceCount ?: 0 } ?: 0
        binding.textHeaderAccumulatedAbsences.text = getString(R.string.accumulated_absences_format, totalAbsences)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
