package com.example.syllabusmarksheetandattendanceregister.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSubjectsTaught(value: HashMap<String, HashMap<String, List<String>>>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSubjectsTaught(value: String): HashMap<String, HashMap<String, List<String>>> {
        val type = object : TypeToken<HashMap<String, HashMap<String, List<String>>>>() {}.type
        return gson.fromJson(value, type) ?: HashMap()
    }

    @TypeConverter
    fun fromAcademicYears(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAcademicYears(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromChapterDataList(value: List<com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toChapterDataList(value: String): List<com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData> {
        val type = object : TypeToken<List<com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
