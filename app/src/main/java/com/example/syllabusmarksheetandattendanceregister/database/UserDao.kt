package com.example.syllabusmarksheetandattendanceregister.database

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_table WHERE id = 0")
    suspend fun getUser(): UserEntity?

    @Query("DELETE FROM user_table")
    suspend fun clearUser()
}
