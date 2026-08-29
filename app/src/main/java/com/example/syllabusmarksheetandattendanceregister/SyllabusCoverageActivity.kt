package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivitySyllabusCoverageBinding

class SyllabusCoverageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySyllabusCoverageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyllabusCoverageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.syllabus_coverage_title)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SyllabusNavFragment())
                .commit()
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, SyllabusCoverageActivity::class.java)
        }
    }
}
