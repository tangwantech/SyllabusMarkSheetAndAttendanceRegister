package com.example.syllabusmarksheetandattendanceregister.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData
import com.example.syllabusmarksheetandattendanceregister.repositories.LoginRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivityViewModel: ViewModel(){
    private val loginRepo = LoginRepository()
    fun loginUser(context: Context, username: String, password: String, deviceId: String, listener: LoginRepository.LoginListener){
        loginRepo.loginUser(username, password, deviceId, object : LoginRepository.LoginListener {
            override fun onLoginSuccessful(userData: UserData) {
                UserRepository.updateUserData(userData)
                viewModelScope.launch {
                    // Use NonCancellable to ensure data is saved even if fragment is destroyed/replaced immediately
                    withContext(NonCancellable) {
                        UserRepository.saveToDatabase(context.applicationContext)
                    }
                    // Call the fragment's listener after the database save attempt
                    listener.onLoginSuccessful(userData)
                }
            }

            override fun onLoginFailed(error: String?) {
                listener.onLoginFailed(error)
            }
        })
    }
}
