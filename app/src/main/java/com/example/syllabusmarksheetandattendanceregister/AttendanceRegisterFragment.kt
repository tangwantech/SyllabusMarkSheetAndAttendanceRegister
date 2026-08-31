package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.StudentsAttendanceAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentAttendanceRegisterBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.AttendanceRegisterRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.AttendanceRegisterViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceRegisterFragment : Fragment(), 
    StudentsAttendanceAdapter.AttendanceChangeListener,
    StudentsAttendanceAdapter.ItemClickListener {

    private var _binding: FragmentAttendanceRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceRegisterViewModel by activityViewModels()
    private lateinit var adapter: StudentsAttendanceAdapter

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val outputFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = StudentsAttendanceAdapter(viewModel.selectedDate.value.toString(), this, this)
        binding.recyclerAttendance.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAttendance.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.students.observe(viewLifecycleOwner) { students ->
            if (students != null) {
                val absenceWeight = viewModel.selectedAbsenceWeight.value?.toIntOrNull() ?: 0
                adapter.updateData(students, absenceWeight)
                updateHeaderStats(students)
            }
        }
        
        binding.textHeaderSubject.text = getString(R.string.marksheet_header_subject_format, viewModel.selectedSubject.value)
        binding.textHeaderClass.text = getString(R.string.absence_weight_format, viewModel.selectedMainClass.value, viewModel.selectedAbsenceWeight.value)
        
        val dateStr = viewModel.selectedDate.value ?: ""
        binding.textHeaderDate.text = try {
            val date = inputFormat.parse(dateStr)
            if (date != null) getString(R.string.attendance_date_format, outputFormat.format(date)) else dateStr
        } catch (e: Exception) {
            getString(R.string.attendance_date_format, dateStr)
        }

        viewModel.navigateToDetailsEvent.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.onNavigatedToDetails()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.attendance_container, StudentAttendanceDetailsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setupListeners() {
        binding.btnSaveAttendance.setOnClickListener {
            viewModel.saveAttendanceRegister(object : AttendanceRegisterRepository.SaveAttendanceRegisterListener {
                override fun onSaveSuccessful(result: String) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Attendance saved successfully", Toast.LENGTH_SHORT).show()
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

    private fun updateHeaderStats(students: List<com.example.syllabusmarksheetandattendanceregister.datamodels.StudentAttendanceData>) {
        val total = students.size
        val date = viewModel.selectedDate.value ?: return
        
        val present = students.count { student ->
            student.attendances?.get(date)?.isPresent == true
        }
        val absent = students.count { student ->
            student.attendances?.get(date)?.isPresent == false
        }
        
        binding.textHeaderStats.text = getString(R.string.attendance_stats_format, total, present, absent)
    }

    override fun onAttendanceChanged(position: Int, isPresent: Boolean) {
        viewModel.updateStudentAttendance(position, isPresent)
    }

    override fun onItemClick(position: Int) {
        viewModel.selectStudent(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
