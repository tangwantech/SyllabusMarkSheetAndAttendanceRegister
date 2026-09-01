package com.example.syllabusmarksheetandattendanceregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentDashboardBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.LogoutRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.MainActivityViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        updateUI()
        setupListeners()
    }

    private fun updateUI() {
        binding.userName.text = viewModel.getUserFullName() ?: "Teacher"
    }

    private fun setupListeners() {
        binding.btnSyllabus.setOnClickListener {
            startActivity(SyllabusCoverageActivity.getIntent(requireContext()))
        }

        binding.btnMarkSheet.setOnClickListener {
            startActivity(MarkSheetActivity.getIntent(requireContext()))
        }

        binding.btnAttendance.setOnClickListener {
            startActivity(AttendanceRegisterActivity.getIntent(requireContext()))
        }

        binding.btnStudentsDatabase.setOnClickListener {
            startActivity(StudentsDatabaseActivity.getIntent(requireContext()))
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        toggleProgress(true)
        viewModel.logoutUser(requireContext(), object : LogoutRepository.LogoutListener {
            override fun onLogoutSuccessful() {
                activity?.runOnUiThread {
                    toggleProgress(false)
                    (activity as? MainActivity)?.showLogin()
                }
            }

            override fun onLogoutFailed(error: String?) {
                activity?.runOnUiThread {
                    toggleProgress(false)
                    Toast.makeText(requireContext(), error ?: "Logout failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun toggleProgress(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}