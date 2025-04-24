package com.example.editecho.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class WhisperRepository(
    private val service: WhisperRetrofitService = WhisperRetrofitService.create(
        com.example.editecho.BuildConfig.OPENAI_API_KEY
    )
) {
    suspend fun transcribe(file: File): String = withContext(Dispatchers.IO) {
<<<<<<< HEAD
        val body = file.asRequestBody("audio/mp3".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        service.transcribe(part)
=======
        try {
            Log.d(TAG, "Starting transcription for file: ${file.name}")
            Log.d(TAG, "File exists: ${file.exists()}, size: ${file.length()} bytes")
            
            // Ensure the file is a valid M4A file
            if (!file.exists() || file.length() == 0L) {
                throw IOException("Invalid audio file")
            }
            
            val body = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                body
            )
            
            Log.d(TAG, "Sending request to Whisper API...")
            val jsonResponse = service.transcribe(part)
            Log.d(TAG, "Received response from Whisper API")
            
            // Parse the JSON response to extract only the text value
            val parsed = try {
                val element = Json.parseToJsonElement(jsonResponse)
                element.jsonObject["text"]?.jsonPrimitive?.content
                    ?: jsonResponse
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse JSON response, returning raw response", e)
                jsonResponse  // fallback if parsing fails
            }
            
            Log.d(TAG, "Parsed transcription: $parsed")
            parsed
        } catch (e: Exception) {
            Log.e(TAG, "Error in transcribe", e)
            Log.e(TAG, "Error details: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
>>>>>>> 9496214 (apikey loading)
    }
} 