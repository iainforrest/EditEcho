package com.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean,
    @SerialName("temperature")
    val temperature: Double = 0.35,
    @SerialName("top_p")
    val topP: Double = 0.95
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
) 