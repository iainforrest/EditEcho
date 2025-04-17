package com.example.editecho.util

import android.util.Log
import com.example.editecho.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Utility class for interacting with the OpenAI API to adjust text tone.
 */
class OpenAIService {
    companion object {
        private const val TAG = "OpenAIService"
    }

    /**
     * Adjusts the tone of the given text using the OpenAI API.
     *
     * @param text The text to adjust
     * @param tone The desired tone (e.g., "Professional", "Casual", "Friendly")
     * @return The adjusted text
     */
    suspend fun adjustTone(text: String, tone: String): String = withContext(Dispatchers.IO) {
        try {
            // Create a client for making HTTP requests
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Create the request body
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a helpful assistant that rewrites text in a $tone tone.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", text)
                })
            }
            
            val jsonBody = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", messages)
            }.toString()
            
            // Create the request
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .post(requestBody)
                .build()
            
            // Make the request
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                val content = message.getString("content")
                
                Log.d(TAG, "Tone adjustment successful")
                return@withContext content
            } else {
                Log.e(TAG, "Error: ${response.code} - ${response.message.orEmpty()}")
                return@withContext "Error adjusting tone: ${response.code} - ${response.message.orEmpty()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting tone: ${e.message}")
            return@withContext "Error adjusting tone: ${e.message}"
        }
    }
} 