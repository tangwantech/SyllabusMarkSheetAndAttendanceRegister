package com.example.syllabusmarksheetandattendanceregister.repositories

import android.content.Context
import com.example.syllabusmarksheetandattendanceregister.database.AppDatabase
import com.example.syllabusmarksheetandattendanceregister.database.UserEntity
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class UserRepository() {
    companion object {
        private var _userData: UserData? = null
        private val _userDataFlow = MutableStateFlow<UserData?>(null)
        val userDataFlow: StateFlow<UserData?> = _userDataFlow
        
        fun updateUserData(userData: UserData){
            this._userData = userData
            _userDataFlow.value = userData
        }

        suspend fun saveToDatabase(context: Context) {
            _userData?.let { userData ->
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(context).userDao().insertUser(UserEntity.fromUserData(userData))
                }
            }
        }

        suspend fun loadFromDatabase(context: Context): UserData? {
            return withContext(Dispatchers.IO) {
                val entity = AppDatabase.getDatabase(context).userDao().getUser()
                val userData = entity?.toUserData()
                _userData = userData
                _userDataFlow.emit(userData) // Use emit to ensure it's picked up
                userData
            }
        }

        suspend fun clearDatabase(context: Context) {
            _userData = null
            _userDataFlow.value = null
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(context).userDao().clearUser()
            }
        }

        fun getUserFullName(): String? {
            return _userData?.fullName
        }

        fun getSessionToken(): String?{
            return _userData?.sessionToken
        }

        fun getAcademicYears(): List<String>?{
            return _userData?.academicYears
        }

        fun getSubjectsTaught(): List<String>?{
            return _userData?.subjectsTaught?.keys?.toList()
        }

        fun getSubjectMainClasses(subjectTaught: String): List<String>?{
            return _userData?.subjectsTaught?.get(subjectTaught)?.keys?.toList()
        }

        fun getSubclasses(subjectTaught: String, mainClass: String): List<String>? {
            return _userData?.subjectsTaught?.get(subjectTaught)?.get(mainClass)
        }
    }
}
