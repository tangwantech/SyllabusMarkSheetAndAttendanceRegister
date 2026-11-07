package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivityHoursCoverageBinding

class HoursCoverageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHoursCoverageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoursCoverageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.hours_coverage)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.hours_coverage_container, HoursCoverageHomeFragment())
                .commit()
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, HoursCoverageActivity::class.java)
        }
    }
}