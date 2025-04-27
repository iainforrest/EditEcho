package com.example.editecho.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 1.0,
    val stream: Boolean
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
) 