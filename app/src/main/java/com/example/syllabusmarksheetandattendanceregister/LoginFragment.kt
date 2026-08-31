package com.example.syllabusmarksheetandattendanceregister

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.syllabusmarksheetandattendanceregister.databinding.FragmentLoginBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData
import com.example.syllabusmarksheetandattendanceregister.repositories.LoginRepository
import com.example.syllabusmarksheetandattendanceregister.utils.NetworkUtils
import com.example.syllabusmarksheetandattendanceregister.viewmodels.LoginActivityViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginActivityViewModel::class.java]
        setupListeners()
    }

    @SuppressLint("HardwareIds")
    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            if (!NetworkUtils.isOnline(requireContext())) {
                NetworkUtils.showNoInternetDialog(requireContext())
                return@setOnClickListener
            }

            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            binding.errorText.visibility = View.GONE

            val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)

            if (username.isNotEmpty() && password.isNotEmpty()) {
                toggleProgress(true)
                loginUser(username, password, deviceId)
            } else {
                binding.errorText.text = "Please enter username and password"
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun loginUser(username: String, password: String, deviceId: String) {
        viewModel.loginUser(requireContext(), username, password, deviceId, object : LoginRepository.LoginListener {
            override fun onLoginSuccessful(userData: UserData) {
                activity?.runOnUiThread {
                    toggleProgress(false)
                    (activity as? MainActivity)?.showDashboard()
                }
            }

            override fun onLoginFailed(error: String?) {
                activity?.runOnUiThread {
                    toggleProgress(false)
                    binding.errorText.text = error ?: "Login failed"
                    binding.errorText.visibility = View.VISIBLE
                }
            }
        })
    }

//    private fun toggleLoading(isLoading: Boolean) {
//        binding.loginProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
//        binding.loginOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
//        binding.loginButton.isEnabled = !isLoading
//        binding.username.isEnabled = !isLoading
//        binding.password.isEnabled = !isLoading
//    }

    private fun toggleProgress(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}