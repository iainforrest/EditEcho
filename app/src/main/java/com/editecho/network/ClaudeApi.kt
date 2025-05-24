package com.editecho.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for Claude API
 */
interface ClaudeApi {
    @POST("messages")
    suspend fun createMessage(@Body request: ClaudeRequest): ClaudeResponse
}

/**
 * Data class for Claude message format
 */
@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

/**
 * Data class for Claude API request
 */
@Serializable
data class ClaudeRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    @SerialName("max_tokens")
    val maxTokens: Int = 4096
)

/**
 * Data class for Claude API response
 */
@Serializable
data class ClaudeResponse(
    val id: String,
    val content: List<ContentBlock>
)

/**
 * Data class for Claude content block
 */
@Serializable
data class ContentBlock(
    val type: String,
    val text: String?
) 