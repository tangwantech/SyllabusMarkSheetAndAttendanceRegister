package com.example.syllabusmarksheetandattendanceregister.repositories

import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LogoutRepository {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()

    fun logoutUser(sessionToken: String, listener: LogoutListener) {
        val params = mapOf(
            "sessionToken" to sessionToken
        )
        val url = "https://parseapi.back4app.com/functions/logout"
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
                listener.onLogoutFailed(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    listener.onLogoutSuccessful()
                } else {
                    val responseBody = response.body?.string() ?: ""
                    val error = try {
                        JSONObject(responseBody).optString("error", "Logout failed")
                    } catch (e: Exception) {
                        "Logout failed"
                    }
                    listener.onLogoutFailed(error)
                }
            }
        })
    }

    interface LogoutListener {
        fun onLogoutSuccessful()
        fun onLogoutFailed(error: String?)
    }
}