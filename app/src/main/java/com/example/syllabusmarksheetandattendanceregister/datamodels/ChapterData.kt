package com.example.syllabusmarksheetandattendanceregister.datamodels



data class ChapterData(
    val chapter: String,
    val module: String,
    val categoryOfActions: String,
    val lessons: List<Lesson>

)

data class Lesson(val lesson: String, var isTaught: Boolean)

data class ChaptersData (val chapters: List<ChapterData>)