package com.example.syllabusmarksheetandattendanceregister

import android.app.Application
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SyllabusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MainScope().launch {
            UserRepository.loadFromDatabase(this@SyllabusApplication)
        }
    }
}
