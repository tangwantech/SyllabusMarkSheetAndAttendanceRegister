package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentHoursCoverageHomeBinding

class HoursCoverageHomeFragment : Fragment() {

    private var _binding: FragmentHoursCoverageHomeBinding? = null
    private val binding get() = _binding!!

    private var selectedWeekNumber: Int? = null
    private var selectedHoursPerWeek: Int? = null
    private var selectedNumberOfSubclasses: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHoursCoverageHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupListeners()
    }

    private fun setupDropdowns() {
        val weekNumbers = (1..33).map { it.toString() }
        val hoursPerWeek = (1..6).map { it.toString() }
        val numberOfSubclasses = (1..4).map { it.toString() }

        binding.spinnerWeekNumber.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, weekNumbers)
        )
        binding.spinnerHoursPerWeek.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, hoursPerWeek)
        )
        binding.spinnerNumberOfSubclasses.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, numberOfSubclasses)
        )
    }

    private fun setupListeners() {
        binding.spinnerWeekNumber.setOnItemClickListener { parent, _, position, _ ->
            selectedWeekNumber = (parent.getItemAtPosition(position) as String).toIntOrNull()
            updateExpectedHours()
        }

        binding.spinnerHoursPerWeek.setOnItemClickListener { parent, _, position, _ ->
            selectedHoursPerWeek = (parent.getItemAtPosition(position) as String).toIntOrNull()
            updateExpectedHours()
        }

        binding.spinnerNumberOfSubclasses.setOnItemClickListener { parent, _, position, _ ->
            selectedNumberOfSubclasses = (parent.getItemAtPosition(position) as String).toIntOrNull()
            updateExpectedHours()
        }

        binding.editActualHoursDone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateHoursCoverage()
            }
        })
    }

    private fun updateExpectedHours() {
        val weekNum = selectedWeekNumber
        val hoursPerWk = selectedHoursPerWeek
        val subclasses = selectedNumberOfSubclasses

        if (weekNum != null && hoursPerWk != null && subclasses != null) {
            val expectedHours = weekNum * hoursPerWk * subclasses
            binding.textExpectedHoursDone.text = getString(R.string.expected_hours_done_format, expectedHours)
            binding.cardExpectedHours.visibility = View.VISIBLE
            binding.layoutActualHoursDone.visibility = View.VISIBLE
            calculateHoursCoverage()
        } else {
            binding.cardExpectedHours.visibility = View.GONE
            binding.layoutActualHoursDone.visibility = View.GONE
            binding.cardHoursStatsTable.visibility = View.GONE
        }
    }

    private fun calculateHoursCoverage() {
        val hoursPerWk = selectedHoursPerWeek ?: return
        val subclasses = selectedNumberOfSubclasses ?: return
        val totalNumberOfHours = 33 * hoursPerWk * subclasses
        val inputStr = binding.editActualHoursDone.text?.toString()?.trim()

        if (inputStr.isNullOrEmpty()) {
            binding.layoutActualHoursDone.error = null
            binding.cardHoursStatsTable.visibility = View.GONE
            return
        }

        val actualHours = inputStr.toIntOrNull()
        if (actualHours != null && actualHours >= 0 && actualHours <= totalNumberOfHours) {
            binding.layoutActualHoursDone.error = null
            val coverage = (actualHours.toDouble() / totalNumberOfHours.toDouble()) * 100.0

            binding.textAnnualHoursProgrammed.text = totalNumberOfHours.toString()
            binding.textActualHoursDoneValue.text = actualHours.toString()
            binding.textPercentageDoneValue.text = getString(R.string.percentage_decimal_format, coverage)

            binding.cardHoursStatsTable.visibility = View.VISIBLE
        } else {
            binding.layoutActualHoursDone.error = getString(R.string.error_actual_hours_invalid, totalNumberOfHours)
            binding.cardHoursStatsTable.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}