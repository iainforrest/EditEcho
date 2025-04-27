package com.example.editecho.network
import com.example.editecho.network.dto.ChatCompletionRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiChatApi {
    @POST("v1/chat/completions")
    suspend fun createCompletion(@Body req: ChatCompletionRequest): ResponseBody
} 