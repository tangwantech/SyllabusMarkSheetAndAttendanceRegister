package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.StudentsDatabaseAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentDeleteStudentsBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.StudentDatabaseRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsDatabaseViewModel

class DeleteStudentsFragment : Fragment() {

    private var _binding: FragmentDeleteStudentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentsDatabaseViewModel by activityViewModels()
    private lateinit var adapter: StudentsDatabaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        
        binding.btnDeleteStudents.setOnClickListener {
            deleteStudents()
        }
    }

    private fun setupRecyclerView() {
        adapter = StudentsDatabaseAdapter(
            isDeleteMode = true,
            onCheckedChangeListener = { index, isChecked ->
                viewModel.toggleStudentSelection(index, isChecked)
            }
        )
        binding.recyclerStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStudents.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.students.observe(viewLifecycleOwner) { students ->
            updateAdapterData()
            val isEmpty = students.isEmpty()
            binding.noDataTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            // Button enablement also depends on having selections, which is handled in studentsToDelete observer
            if (isEmpty) binding.btnDeleteStudents.isEnabled = false
        }

        viewModel.studentsToDelete.observe(viewLifecycleOwner) { selectedList ->
            binding.textHeaderStats.text = "Selected: ${selectedList.size} student(s)"
            updateAdapterData()
            binding.btnDeleteStudents.isEnabled = selectedList.isNotEmpty()
        }
    }

    private fun updateAdapterData() {
        val students = viewModel.students.value ?: emptyList()
        val selectedMatricules = viewModel.studentsToDelete.value?.map { it.matricule }?.toSet() ?: emptySet()
        val selectedIndices = mutableSetOf<Int>()
        students.forEachIndexed { index, student ->
            if (selectedMatricules.contains(student.matricule)) {
                selectedIndices.add(index)
            }
        }
        adapter.updateData(students, selectedIndices)
    }

    private fun deleteStudents() {
        viewModel.deleteSelectedStudents(object : StudentDatabaseRepository.DeleteStudentsListener {
            override fun onDeleteSuccessful(result: String) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Students deleted successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }

            override fun onError(error: String) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to delete students: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearStudentsToDelete()
        _binding = null
    }
}