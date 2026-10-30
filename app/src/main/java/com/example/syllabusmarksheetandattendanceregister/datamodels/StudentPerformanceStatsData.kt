package com.example.syllabusmarksheetandattendanceregister.datamodels

data class StudentPerformanceStatsData(val males: Stats, val females: Stats,
    val overall: Stats)
data class Stats(val numSat: Int, val numPassed: Int, val percentPassed: Double)
