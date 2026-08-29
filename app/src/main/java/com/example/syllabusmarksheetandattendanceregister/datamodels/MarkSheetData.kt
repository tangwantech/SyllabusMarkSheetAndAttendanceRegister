package com.example.syllabusmarksheetandattendanceregister.datamodels

data class MarkSheetData(val students: List<Student>)
data class Student(val name: String, val matricule: String, val gender: String, var isRegistered: Boolean, var score: Double)