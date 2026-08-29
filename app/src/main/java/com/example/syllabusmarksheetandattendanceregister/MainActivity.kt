package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivityMainBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            checkSession()
        }
    }

    private fun checkSession() {
        lifecycleScope.launch {
            val userData = UserRepository.loadFromDatabase(this@MainActivity)
            if (userData?.sessionToken != null) {
                showDashboard()
            } else {
                showLogin()
            }
        }
    }

    fun showLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    fun showDashboard() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DashboardFragment())
            .commit()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}