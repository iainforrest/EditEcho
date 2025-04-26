// OpenAiAssistantsApi.kt
package com.example.editecho.network

import com.example.editecho.network.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OpenAiAssistantsApi {

    @POST("threads")
    suspend fun createThread(
        @Body body: Map<String, String> = emptyMap()
    ): ThreadResponse

    @POST("threads/{thread_id}/messages")
    suspend fun addMessage(
        @Path("thread_id") threadId: String,
        @Body body: MessageRequestBody
    ): MessageResponse

    @POST("threads/{thread_id}/runs")
    suspend fun createRun(
        @Path("thread_id") threadId: String,
        @Body body: RunRequestBody
    ): RunResponse

    @GET("threads/{thread_id}/messages")
    suspend fun listMessages(
        @Path("thread_id") threadId: String
    ): MessagesListResponse
}
