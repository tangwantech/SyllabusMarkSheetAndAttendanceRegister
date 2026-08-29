package com.example.syllabusmarksheetandattendanceregister.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyllabusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabus(syllabus: SyllabusEntity)

    @Query("SELECT * FROM syllabus_coverage WHERE id = 'current_syllabus'")
    suspend fun getSyllabus(): SyllabusEntity?

    @Query("DELETE FROM syllabus_coverage")
    suspend fun clearSyllabus()
}
