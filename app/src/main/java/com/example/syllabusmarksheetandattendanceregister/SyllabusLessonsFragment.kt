package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.LessonCheckChangeListener
import com.example.syllabusmarksheetandattendanceregister.adapters.LessonsAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentSyllabusLessonsBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.SyllabusViewModel

class SyllabusLessonsFragment : Fragment(), LessonCheckChangeListener {

    private var _binding: FragmentSyllabusLessonsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyllabusViewModel by activityViewModels()
    private lateinit var adapter: LessonsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyllabusLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = LessonsAdapter(this)
        binding.recyclerLessons.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLessons.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.selectedChapter.observe(viewLifecycleOwner) { it ->
            if (it != null) {
                binding.textChapterTitle.text = getString(R.string.chapter_title_format, it.chapter)
                adapter.updateData(it.lessons)
//                viewModel.updateSelectedSubjectLessonsCount(it.lessons.count {lesson -> lesson.isTaught })
            }
        }

        viewModel.selectChapterLessonsCount.observe(viewLifecycleOwner){ count ->
            val total = viewModel.selectedChapter.value?.lessons?.size ?: 0
            binding.textLessonStats.text = getString(R.string.lesson_stats_format, count, total)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSelectedSubjectLessonsCount()

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLessonCheckChange(lessonIndex: Int, isChecked: Boolean) {
        viewModel.updateLessonStatus(lessonIndex, isChecked)
    }


}
