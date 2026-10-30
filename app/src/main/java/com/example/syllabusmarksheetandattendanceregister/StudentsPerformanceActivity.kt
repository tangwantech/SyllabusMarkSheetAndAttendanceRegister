package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivityStudentsPerformanceBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsPerformanceViewModel
import kotlinx.coroutines.launch

class StudentsPerformanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentsPerformanceBinding
    private lateinit var viewModel: StudentsPerformanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentsPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.students_performance)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            UserRepository.loadFromDatabase(this@StudentsPerformanceActivity)
            viewModel.loadInitialData()

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.students_performance_container, StudentsPerformanceNavFragment())
                    .commit()
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[StudentsPerformanceViewModel::class.java]
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, StudentsPerformanceActivity::class.java)
        }
    }
}