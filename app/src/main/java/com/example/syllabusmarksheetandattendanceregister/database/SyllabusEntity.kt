package com.example.syllabusmarksheetandattendanceregister.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData

@Entity(tableName = "syllabus_coverage")
data class SyllabusEntity(
    @PrimaryKey val id: String = "current_syllabus",
    val academicYear: String,
    val subject: String,
    val mainClass: String,
    val chapters: List<ChapterData>
)
