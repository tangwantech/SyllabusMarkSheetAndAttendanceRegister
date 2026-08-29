package com.example.syllabusmarksheetandattendanceregister.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: Int = 0, // Single user storage
    val fullName: String,
    val sessionToken: String?,
    val isOnline: Boolean,
    val subjectsTaught: HashMap<String, HashMap<String, List<String>>>,
    val academicYears: List<String>
) {
    fun toUserData(): UserData {
        return UserData(
            fullName = fullName,
            sessionToken = sessionToken,
            isOnline = isOnline,
            subjectsTaught = subjectsTaught,
            academicYears = academicYears
        )
    }

    companion object {
        fun fromUserData(userData: UserData): UserEntity {
            return UserEntity(
                fullName = userData.fullName,
                sessionToken = userData.sessionToken,
                isOnline = userData.isOnline,
                subjectsTaught = userData.subjectsTaught,
                academicYears = userData.academicYears
            )
        }
    }
}
