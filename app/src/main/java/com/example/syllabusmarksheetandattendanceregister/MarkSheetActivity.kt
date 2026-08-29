package com.example.syllabusmarksheetandattendanceregister

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.syllabusmarksheetandattendanceregister.databinding.ActivityMarkSheetBinding

class MarkSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.marksheet)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mark_sheet_container, MarkSheetNavFragment())
                .commit()
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, MarkSheetActivity::class.java)
        }
    }
}