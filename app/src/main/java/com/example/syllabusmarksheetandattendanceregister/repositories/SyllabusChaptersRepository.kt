package com.example.syllabusmarksheetandattendanceregister.repositories


import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChaptersData
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

class SyllabusChaptersRepository {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()
//    private var chapters: List<ChapterData>? = null

    fun getSyllabusCoverage(sessionToken: String, academicYear: String, subject: String, mainClass: String, listener: GetSyllabusListener){
        val params = hashMapOf<String, String>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass
        )

        val url = "https://parseapi.back4app.com/functions/getSyllabusChapters"
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
                listener.onError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
                    val result = JSONObject(responseBody)["result"].toString()
                    val chaptersData = Gson().fromJson<ChaptersData>(result, ChaptersData::class.java)
//                    println(chaptersData)
                    listener.onSyllabusAvailable(chaptersData.chapters)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })

    }




    fun updateSyllabusCoverage(
        sessionToken: String,
        academicYear: String,
        subject: String,
        mainClass: String,
        chaptersData: ChaptersData,
        listener: UpdateSyllabusListener
    ) {

        val chapsData = Gson().toJson(chaptersData).toString()
        val params = hashMapOf<String, String>(
            "sessionToken" to sessionToken,
            "academicYear" to academicYear,
            "subject" to subject,
            "mainClass" to mainClass,
            "chaptersData" to chapsData,

        )

        val url = "https://parseapi.back4app.com/functions/updateSyllabusChapters"
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
                listener.onError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    listener.onUpdateSyllabusSuccessful()
                } else {
                    listener.onError(response.body?.string() ?: "Update failed")
                }
            }
        })
    }

    interface GetSyllabusListener{
        fun onSyllabusAvailable(chapters:List<ChapterData>)
        fun onError(error: String?)
    }

    interface UpdateSyllabusListener{
        fun onUpdateSyllabusSuccessful()
        fun onError(error: String?)
    }
}