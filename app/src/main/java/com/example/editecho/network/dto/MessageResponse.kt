package com.example.editecho.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val id: String,
    val role: String,
    val content: List<ContentBlock>
) {
    val text: String
        get() = content
            .firstOrNull { it.type == "text" }
            ?.text
            ?.value
            ?: ""
}

@JsonClass(generateAdapter = true)
data class ContentBlock(
    val type: String,
    val text: TextValue
)

@JsonClass(generateAdapter = true)
data class TextValue(
    val value: String,
    val annotations: List<Any> = emptyList()
) 