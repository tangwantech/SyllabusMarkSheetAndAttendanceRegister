package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentStudentsPerformanceBinding
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsPerformanceViewModel

class StudentsPerformanceFragment : Fragment() {

    private var _binding: FragmentStudentsPerformanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentsPerformanceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.performanceData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.textHeaderSubject.text = getString(R.string.marksheet_header_subject_format, viewModel.selectedSubject.value)
                binding.textHeaderSubclass.text = getString(R.string.marksheet_header_subclass_format, viewModel.selectedMainClass.value)
                binding.textHeaderSequence.text = viewModel.selectedSequence.value

                val maleStats = data.males
                val femaleStats = data.females
                val overallStats = data.overall

                if (maleStats != null) {
                    binding.textMaleEnrolled.text = maleStats.numSat.toString()
                    binding.textMalePassed.text = maleStats.numPassed.toString()
                    binding.textMalePercentage.text = getString(R.string.percentage_decimal_format, maleStats.percentPassed)
                } else {
                    binding.textMaleEnrolled.text = "0"
                    binding.textMalePassed.text = "0"
                    binding.textMalePercentage.text = getString(R.string.percentage_decimal_format, 0.0)
                }

                if (femaleStats != null) {
                    binding.textFemaleEnrolled.text = femaleStats.numSat.toString()
                    binding.textFemalePassed.text = femaleStats.numPassed.toString()
                    binding.textFemalePercentage.text = getString(R.string.percentage_decimal_format, femaleStats.percentPassed)
                } else {
                    binding.textFemaleEnrolled.text = "0"
                    binding.textFemalePassed.text = "0"
                    binding.textFemalePercentage.text = getString(R.string.percentage_decimal_format, 0.0)
                }

                if (overallStats != null) {
                    binding.textTotalEnrolled.text = overallStats.numSat.toString()
                    binding.textTotalPassed.text = overallStats.numPassed.toString()
                    binding.textTotalPercentage.text = getString(R.string.percentage_decimal_format, overallStats.percentPassed)
                } else {
                    binding.textTotalEnrolled.text = "0"
                    binding.textTotalPassed.text = "0"
                    binding.textTotalPercentage.text = getString(R.string.percentage_decimal_format, 0.0)
                }

                binding.noDataTextView.visibility = View.GONE
            } else {
                binding.noDataTextView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}