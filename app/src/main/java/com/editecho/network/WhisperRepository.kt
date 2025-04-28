package com.editecho.network

import android.util.Log
import com.editecho.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class WhisperRepository(
    private val service: WhisperRetrofitService = WhisperRetrofitService.create(
        BuildConfig.OPENAI_API_KEY
    )
) {
    companion object {
        private const val TAG = "WhisperRepository"
    }

    init {
        // Initialize any necessary components
    }

    suspend fun transcribe(file: File): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting transcription for file: ${file.name}")
            Log.d(TAG, "File exists: ${file.exists()}, size: ${file.length()} bytes")
            
            // Ensure the file is a valid audio file
            if (!file.exists() || file.length() == 0L) {
                throw IOException("Invalid audio file")
            }
            
            // Use the correct media type based on the file extension
            val mediaType = when {
                file.name.endsWith(".mp3", ignoreCase = true) -> "audio/mp3"
                file.name.endsWith(".m4a", ignoreCase = true) -> "audio/m4a"
                else -> "audio/mp3" // Default to mp3 if extension is unknown
            }
            
            val body = file.asRequestBody(mediaType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                body
            )
            
            // Create the model RequestBody
            val modelRequestBody = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
            
            Log.d(TAG, "Sending request to Whisper API...")
            val response = service.transcribe(part, modelRequestBody)
            Log.d(TAG, "Received response from Whisper API")
            
            Log.d(TAG, "Transcription: ${response.text}")
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Error in transcribe", e)
            Log.e(TAG, "Error details: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
} 