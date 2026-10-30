package com.example.syllabusmarksheetandattendanceregister.repositories

import android.util.Log
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import com.example.syllabusmarksheetandattendanceregister.datamodels.MarkSheetData
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentPerformanceStatsData
import com.example.syllabusmarksheetandattendanceregister.repositories.MarkSheetRepository.FetchMarksheetListener
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class StudentsPerformanceStatsRepo {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()
    fun fetchStudentsPerformanceStats(sessionToken: String, academicYear: String, subject: String, mainClass: String, sequence: String, listener: StudentsPerformanceStatsListener){
        val params = hashMapOf<String, String>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "sequence" to sequence
        )

        val url = "https://parseapi.back4app.com/functions/fetchStudentsPerformanceStats"
        val requestBody = JSONObject(params).toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onError(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
                    val result = JSONObject(responseBody)["result"].toString()
                    val stats = JSONObject(result)["studentsPerformanceStats"].toString()
                    val studentPerformanceStatsData = Gson().fromJson<StudentPerformanceStatsData>(stats,
                        StudentPerformanceStatsData::class.java)

//                    Log.d("StudentsPerformanceStatsData", studentPerformanceStatsData.toString())
                    listener.onStudentsPerformanceStatsFetched(studentPerformanceStatsData)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })

    }

    interface StudentsPerformanceStatsListener{
        fun onStudentsPerformanceStatsFetched(result: StudentPerformanceStatsData)
        fun onError(error: String)
    }

}