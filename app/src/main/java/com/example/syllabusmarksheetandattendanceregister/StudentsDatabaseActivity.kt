package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivityStudentsDatabaseBinding
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import com.example.syllabusmarksheetandattendanceregister.viewmodels.StudentsDatabaseViewModel
import kotlinx.coroutines.launch

class StudentsDatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentsDatabaseBinding
    private lateinit var viewModel: StudentsDatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentsDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[StudentsDatabaseViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.students_database_title)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            UserRepository.loadFromDatabase(this@StudentsDatabaseActivity)
            viewModel.loadInitialData()

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.students_database_container, StudentsDatabaseNavFragment())
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
            return Intent(context, StudentsDatabaseActivity::class.java)
        }
    }
}