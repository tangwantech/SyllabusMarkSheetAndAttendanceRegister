package com.example.syllabusmarksheetandattendanceregister.datamodels

data class UserData(
    var fullName: String,
    var sessionToken: String?,
    var isOnline: Boolean,
    var subjectsTaught: HashMap<String, HashMap<String, List<String>>>,
    var academicYears: List<String>
)
