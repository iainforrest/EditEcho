package com.example.editecho.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WhisperApiService {
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Header("Authorization") authorization: String
    ): WhisperResponse
}

data class WhisperResponse(
    val text: String
) 