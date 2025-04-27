package com.example.editecho.network

import com.example.editecho.network.dto.WhisperResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

private const val BASE_URL = "https://api.openai.com/"

interface WhisperRetrofitService {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): WhisperResponse

    companion object {
        fun create(apiKey: String): WhisperRetrofitService {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $apiKey")
                            .build()
                    )
                }
                .build()

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(WhisperRetrofitService::class.java)
        }
    }
} 