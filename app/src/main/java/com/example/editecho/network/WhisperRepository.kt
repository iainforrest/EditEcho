package com.example.editecho.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val body = file.asRequestBody("audio/mp3".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        service.transcribe(part)
    }
} 