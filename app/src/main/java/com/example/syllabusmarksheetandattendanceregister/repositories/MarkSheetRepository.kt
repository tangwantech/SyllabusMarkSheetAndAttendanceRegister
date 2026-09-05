package com.example.syllabusmarksheetandattendanceregister.repositories

import android.util.Log
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import com.example.syllabusmarksheetandattendanceregister.datamodels.MarkSheetData
import com.example.syllabusmarksheetandattendanceregister.datamodels.Student
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

class MarkSheetRepository {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()

    fun fetchMarkSheet(sessionToken: String, academicYear: String, subject: String, mainClass: String, subclass: String, sequence: String, listener: FetchMarksheetListener){
        val params = hashMapOf<String, String>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "subclass" to subclass,
            "sequence" to sequence
        )

        val url = "https://parseapi.back4app.com/functions/fetchMarkSheet"
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
                    val markSheetData = Gson().fromJson<MarkSheetData>(result, MarkSheetData::class.java)
                    println("Marksheet from server: $markSheetData")
                    Log.d("MarksheetRepoFetch", markSheetData.toString())
                    listener.onMarkSheetAvailable(markSheetData)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })

    }

    fun updateMarkSheet(sessionToken: String, academicYear: String, subject: String,
                        mainClass: String, subclass: String, sequence: String,
                        students: List<Student>, listener: UpdateMarkSheetListener){

        val tempStd = toListHashMap(students)

//        println("Students before update to server: $tempStd")
//        Log.d("MarksheetUpdate", tempStd.toString())
        val params = hashMapOf<String, Any>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "subclass" to subclass,
            "sequence" to sequence,
            "students" to tempStd
        )


        val url = "https://parseapi.back4app.com/functions/updateMarkSheet"
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

                    listener.onUpdateSuccessful(result)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }


    fun toListHashMap(data: List<Student>): List<HashMap<String, Any>>{
        val students = mutableListOf<HashMap<String, Any>>()
        for (std in data){
            val temp = hashMapOf<String, Any>()
            temp["name"] = std.name
            temp["matricule"] = std.matricule
            temp["gender"] = std.gender
            temp["isRegistered"] = std.isRegistered
            temp["score"] = std.score
            students.add(temp)
        }
        return students
    }

    interface FetchMarksheetListener{
        fun onMarkSheetAvailable(result: MarkSheetData)
        fun onError(error:String)
    }

    interface UpdateMarkSheetListener {
        fun onUpdateSuccessful(result: String)
        fun onError(error: String)
    }
}

