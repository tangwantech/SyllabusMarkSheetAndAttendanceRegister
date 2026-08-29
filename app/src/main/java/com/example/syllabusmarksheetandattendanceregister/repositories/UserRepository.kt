package com.example.syllabusmarksheetandattendanceregister.repositories

import android.content.Context
import com.example.syllabusmarksheetandattendanceregister.database.AppDatabase
import com.example.syllabusmarksheetandattendanceregister.database.UserEntity
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository() {
    companion object {
        private var _userData: UserData? = null
        
        fun updateUserData(userData: UserData){
            this._userData = userData
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
                _userData = entity?.toUserData()
                _userData
            }
        }

        suspend fun clearDatabase(context: Context) {
            _userData = null
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
