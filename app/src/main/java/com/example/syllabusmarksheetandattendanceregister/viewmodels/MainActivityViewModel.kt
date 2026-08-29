package com.example.syllabusmarksheetandattendanceregister.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.repositories.LogoutRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {

    fun getUserFullName(): String? {
        return UserRepository.getUserFullName()
    }

    fun logoutUser(context: Context, listener: LogoutRepository.LogoutListener) {
        val sessionToken = UserRepository.getSessionToken()
        if (sessionToken != null) {
            val logoutRepository = LogoutRepository()
            logoutRepository.logoutUser(sessionToken, listener)
            
            viewModelScope.launch {
                UserRepository.clearDatabase(context.applicationContext)
            }
        } else {
            listener.onLogoutFailed("No user credentials found")
        }
    }
}
