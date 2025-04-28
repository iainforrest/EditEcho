package com.editecho.network
import com.editecho.network.dto.ChatCompletionRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiChatApi {
    @POST("chat/completions")
    suspend fun createCompletion(@Body req: ChatCompletionRequest): ResponseBody
} 