package com.example.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MessageResponse(
    @SerialName("id")
    val id: String,
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: List<ContentBlock>
) {
    val text: String
        get() = content
            .firstOrNull { it.type == "text" }
            ?.text
            ?.value
            ?: ""
}

@Serializable
data class ContentBlock(
    @SerialName("type")
    val type: String,
    @SerialName("text")
    val text: TextValue
)

@Serializable
data class TextValue(
    @SerialName("value")
    val value: String,
    @SerialName("annotations")
    val annotations: List<JsonElement> = emptyList()
) 