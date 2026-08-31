package com.example.syllabusmarksheetandattendanceregister.datamodels

data class StudentsAttendanceData(val students: List<StudentAttendanceData>)
data class StudentAttendanceData(val name: String, val matricule: String, val gender: String, val isRegistered: Boolean, var attendances: HashMap<String, Attendance>?)
data class Attendance(var isPresent: Boolean? = null, var absenceCount: Int? = null)
