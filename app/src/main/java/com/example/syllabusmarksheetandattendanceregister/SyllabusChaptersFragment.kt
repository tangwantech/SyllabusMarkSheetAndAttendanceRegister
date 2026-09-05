package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.syllabusmarksheetandattendanceregister.adapters.ChapterClickListener
import com.example.syllabusmarksheetandattendanceregister.adapters.ChaptersAdapter
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentSyllabusChaptersBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData
import com.example.syllabusmarksheetandattendanceregister.repositories.SyllabusChaptersRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.SyllabusViewModel

class SyllabusChaptersFragment : Fragment(), ChapterClickListener {

    private var _binding: FragmentSyllabusChaptersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyllabusViewModel by activityViewModels()
    private lateinit var adapter: ChaptersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyllabusChaptersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateIsLoading(false)
    }

    private fun setupRecyclerView() {
        val chapters = viewModel.chapters.value ?: emptyList()
        adapter = ChaptersAdapter(chapters, this)
        binding.recyclerChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChapters.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.chapters.observe(viewLifecycleOwner) { chapters ->
            if (chapters != null) {
                binding.textSubject.text = getString(R.string.marksheet_header_subject_format, viewModel.selectedSubject.value)
                binding.textClass.text = getString(R.string.marksheet_header_subclass_format, viewModel.selectedMainClass.value)
                adapter.updateChapters(chapters)
            }
            val isEmpty = chapters.isNullOrEmpty()
            binding.noDataTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.btnSaveSyllabus.isEnabled = !isEmpty && (viewModel.isLoading.value == false)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            toggleProgress(isLoading)
            binding.btnSaveSyllabus.isEnabled = !isLoading
        }

        viewModel.overallProgress.observe(viewLifecycleOwner) { progress ->
            binding.textOverallLessonStats.text = getString(
                R.string.overall_lesson_stats_format,
                progress.completedLessons,
                progress.totalLessons
            )
            binding.overallProgressIndicator.max = 100
            binding.overallProgressIndicator.progress = progress.percentage.toInt()
            binding.textOverallPercentage.text = getString(R.string.percentage_decimal_format, progress.percentage)
        }
    }

    private fun setupListeners() {
        binding.btnSaveSyllabus.setOnClickListener {
            viewModel.updateChaptersInSyllabusChaptersRepository(requireContext().applicationContext, object : SyllabusChaptersRepository.UpdateSyllabusListener {
                override fun onUpdateSyllabusSuccessful() {
                    activity?.runOnUiThread {
                        viewModel.updateIsLoading(false)
                        Toast.makeText(requireContext(), getString(R.string.update_successful), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: String?) {
                    activity?.runOnUiThread {
                        viewModel.updateIsLoading(false)
                        Toast.makeText(requireContext(), error ?: "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onChapterClick(chapterIndex: Int, chapter: ChapterData) {
        viewModel.updateSelectedChapterIndexAndChapter(chapterIndex, chapter)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SyllabusLessonsFragment())
            .addToBackStack(null)
            .commit()
    }
    private fun toggleProgress(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

}
