package com.example.syllabusmarksheetandattendanceregister.repositories

import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.APPLICATION_ID
import com.example.syllabusmarksheetandattendanceregister.credentials.Credentials.Companion.CLIENT_KEY
import com.example.syllabusmarksheetandattendanceregister.datamodels.UserData

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

class LoginRepository {
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()
    fun loginUser(username: String, password: String, deviceId: String, listener: LoginListener, userDataListener: UserDataListener){
        val params = mapOf(
            "username" to username,
            "password" to password,
            "deviceId" to deviceId
        )
        val url = "https://parseapi.back4app.com/functions/login"
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
                println(e.message.toString())
                listener.onLoginFailed(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val result = JSONObject(responseBody)["result"].toString()
                        println(result)

                        listener.onLoginSuccessful()
                        val userData = Gson().fromJson<UserData>(result, UserData::class.java)
                        userDataListener.onUserDataReceived(userData)
                    } catch (e: Exception) {
                        println(e.message.toString())
                        listener.onLoginFailed(e.message.toString())
                    }
                } else {
                    val error = try {
                        val json = JSONObject(responseBody)
                        if (json.has("error")) json.getString("error")
                        else "Login failed: Invalid credentials"
                    } catch (e: Exception) {
                        println(e.message.toString())
                        e.message.toString()
                    }
                    listener.onLoginFailed(error)
                }
            }
        })
    }

    interface LoginListener {
        fun onLoginSuccessful()
        fun onLoginFailed(error: String?)
    }

    interface UserDataListener {
        fun onUserDataReceived(userData: UserData)
    }


}