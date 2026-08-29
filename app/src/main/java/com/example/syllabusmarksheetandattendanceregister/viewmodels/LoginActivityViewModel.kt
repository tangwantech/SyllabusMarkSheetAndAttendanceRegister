package com.example.syllabusmarksheetandattendanceregister.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData
import com.example.syllabusmarksheetandattendanceregister.repositories.LoginRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.launch

class LoginActivityViewModel: ViewModel(){
    private val loginRepo = LoginRepository()
    fun loginUser(context: Context, username: String, password: String, deviceId: String, listener: LoginRepository.LoginListener){
//        println("User login... in LoginActivityViewmodel")
        loginRepo.loginUser(username, password, deviceId, listener, object: LoginRepository.UserDataListener {
            override fun onUserDataReceived(userData: UserData) {
//                println("User data in login viewmodel: $userData")
                UserRepository.updateUserData(userData)
                viewModelScope.launch {
                    UserRepository.saveToDatabase(context.applicationContext)
                }
            }
        })
    }
}
