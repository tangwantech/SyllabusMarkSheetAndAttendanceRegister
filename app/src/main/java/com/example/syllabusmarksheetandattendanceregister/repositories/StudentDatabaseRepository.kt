package com.example.syllabusmarksheetandattendanceregister.repositories

import android.util.Log
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentData
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentsData
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



class StudentDatabaseRepository {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun fetchStudents(sessionToken: String, academicYear: String, subject: String, mainClass: String, subclass: String, listener: FetchStudentsListener){
        val params = hashMapOf<String, String>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "subclass" to subclass
        )

        val url = "https://parseapi.back4app.com/functions/fetchStudentsV2"
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
                    try {
                        val result = JSONObject(responseBody)["result"].toString()
                        val studentsData = Gson().fromJson(result, StudentsData::class.java)
                        Log.d("Students", studentsData.students.toString())
                        listener.onStudentsAvailable(studentsData.students)
                    } catch (e: Exception) {
                        listener.onError("Failed to parse response")
                    }
                } else {
                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }

    interface FetchStudentsListener {
        fun onStudentsAvailable(students: List<StudentData>)
        fun onError(error: String)
    }

    fun addStudents(sessionToken: String, academicYear: String, subject: String, mainClass: String, subclass: String, students: List<StudentData>, listener: AddStudentsListener){
        val studs = JSONObject(Gson().toJson(StudentsData(students)))
        val params = hashMapOf<String, Any>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "subclass" to subclass,
            "studentsData" to studs
        )

        val url = "https://parseapi.back4app.com/functions/addStudentsV1"
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
                    Log.d("StudentsAdded", result)
                    listener.onStudentsAdded(result)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }


    interface AddStudentsListener{
        fun onStudentsAdded(result: String)
        fun onError(error: String)
    }

    fun deleteStudents(sessionToken: String, studentsToDelete: List<StudentData>, listener: DeleteStudentsListener) {

        val students = JSONObject(Gson().toJson(StudentsData(studentsToDelete)))
        val params = hashMapOf<String, Any>(
            "sessionToken" to sessionToken,
            "studentsToDelete" to students
        )

        val url = "https://parseapi.back4app.com/functions/deleteStudents"
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
                    listener.onDeleteSuccessful(result)
                } else {
                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }

    interface DeleteStudentsListener {
        fun onDeleteSuccessful(result: String)
        fun onError(error: String)
    }
}