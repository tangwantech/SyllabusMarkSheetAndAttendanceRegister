package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivityAttendanceRegisterBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.AttendanceRegisterViewModel
import kotlinx.coroutines.launch

class AttendanceRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceRegisterBinding
    private lateinit var viewModel: AttendanceRegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AttendanceRegisterViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.attendance_register)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            UserRepository.loadFromDatabase(this@AttendanceRegisterActivity)
            viewModel.loadInitialData()

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.attendance_container, AttendanceNavFragment())
                    .commit()
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AttendanceRegisterActivity::class.java)
        }
    }
}
