package com.example.syllabusmarksheetandattendanceregister.repositories

import android.util.Log
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import com.example.syllabusmarksheetandattendanceregister.datamodels.Attendance
import com.example.syllabusmarksheetandattendanceregister.datamodels.MarkSheetData
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentAttendanceData
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentsAttendanceData
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

class AttendanceRegisterRepository() {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()

    fun fetchAttendanceRegister(sessionToken: String, academicYear: String,
                                mainClass: String, subclass: String, subject: String,
                                selectedDate: String, listener: FetchAttendanceRegisterListener){

        val params = hashMapOf<String, String>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "subclass" to subclass,
            "selectedDate" to selectedDate
        )
        val url = "https://parseapi.back4app.com/functions/fetchAttendanceRegister"
        val requestBody = JSONObject(params).toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

//        Log.d("AttendanceRegisterRepo", "Fetching attendance register")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("AttendanceRegisterRepo", e.message.toString())
                listener.onError(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
                    val result = JSONObject(responseBody)["result"].toString()
                    println(result)
                    val studentsAttendance =  Gson().fromJson(result, StudentsAttendanceData::class.java)
                    Log.d("AttendanceRegisterRepo", studentsAttendance.toString())
                    listener.onFetchSuccessful(studentsAttendance.students)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })


    }

    fun saveAttendanceRegister(sessionToken: String, academicYear: String,
                               mainClass: String, subclass: String, subject: String,
                               studentsAttendanceData: StudentsAttendanceData,
                               listener: SaveAttendanceRegisterListener){

        val jsonObject = JSONObject(Gson().toJson(studentsAttendanceData))
        val params = hashMapOf<String, Any>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "subclass" to subclass,
            "studentsAttendanceData" to jsonObject
        )
        val url = "https://parseapi.back4app.com/functions/saveAttendanceRegister"
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
                Log.d("SaveAttendanceRegisterOnFailure", e.message.toString())
                listener.onError(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
                    val result = JSONObject(responseBody)["result"].toString()
                    Log.d("SaveAttendanceRegisterOnSuccess", result)
                    listener.onSaveSuccessful(result)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })

    }

    interface FetchAttendanceRegisterListener{
        fun onFetchSuccessful(students:List<StudentAttendanceData>)
        fun onError(error:String)
    }

    interface SaveAttendanceRegisterListener{
        fun onSaveSuccessful(result: String)
        fun onError(error:String)
    }
}