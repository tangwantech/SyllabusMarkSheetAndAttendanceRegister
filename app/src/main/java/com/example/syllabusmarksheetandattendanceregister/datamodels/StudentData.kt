package com.example.syllabusmarksheetandattendanceregister.datamodels


data class StudentsData(val students: List<StudentData>)
data class StudentData(
    var academicYear: String, var name: String,
    var matricule: String, var gender: String,
    var subject: String, var mainClass: String, var subclass: String
)
