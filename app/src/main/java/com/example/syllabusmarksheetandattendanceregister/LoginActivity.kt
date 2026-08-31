package com.example.syllabusmarksheetandattendanceregister

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.syllabusmarksheetandattendanceregister.databinding.LoginBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData
import com.example.syllabusmarksheetandattendanceregister.repositories.LoginRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import com.example.syllabusmarksheetandattendanceregister.utils.NetworkUtils
import com.example.syllabusmarksheetandattendanceregister.viewmodels.LoginActivityViewModel
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: LoginBinding
    private lateinit var viewmodel: LoginActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        checkExistingUser()
        setupListeners()
    }

    private fun checkExistingUser() {
        binding.loginOverlay.visibility = View.VISIBLE
        binding.loginProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val userData = UserRepository.loadFromDatabase(this@LoginActivity)
            if (userData?.sessionToken != null) {
                gotoMainActivity()
            } else {
                binding.loginOverlay.visibility = View.GONE
                binding.loginProgress.visibility = View.GONE
            }
        }
    }

    fun setupViewModel(){
        viewmodel = ViewModelProvider(this)[LoginActivityViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        binding.username.text?.clear()
        binding.password.text?.clear()
        binding.errorText.visibility = View.GONE
        toggleLoading(false)
    }

    private fun loginUser(username: String, password: String, deviceId: String){
        viewmodel.loginUser(this, username, password, deviceId, object : LoginRepository.LoginListener{
            override fun onLoginSuccessful(userData: UserData) {
                runOnUiThread {
                    toggleLoading(false)
                    gotoMainActivity()
                }
            }

            override fun onLoginFailed(error: String?) {
                runOnUiThread {
                    toggleLoading(false)
                    binding.errorText.text = error ?: "Login failed"
                    binding.errorText.visibility = View.VISIBLE
                }
            }
        })
    }

    @SuppressLint("HardwareIds")
    private fun setupListeners(){
        binding.loginButton.setOnClickListener {
            if (!NetworkUtils.isOnline(this)) {
                NetworkUtils.showNoInternetDialog(this)
                return@setOnClickListener
            }

            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            binding.errorText.visibility = View.GONE

            // Unique Device ID to enforce single-session login
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            if (username.isNotEmpty() && password.isNotEmpty()) {
                toggleLoading(true)
                loginUser(username, password, deviceId)
            } else {
                binding.errorText.text = "Please enter username and password"
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.loginProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
        binding.username.isEnabled = !isLoading
        binding.password.isEnabled = !isLoading
    }

    private fun gotoMainActivity(){
        startActivity(MainActivity.getIntent(this))
    }
}