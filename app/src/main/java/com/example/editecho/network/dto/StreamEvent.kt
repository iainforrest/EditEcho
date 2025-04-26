package com.example.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StreamEvent(
    val event: String,
    val data: String
) {
    fun parseData(): StreamEventData? {
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString<StreamEventData>(data)
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class StreamEventData(
    val id: String,
    val type: String,
    val delta: Delta? = null,
    @SerialName("object")
    val objectType: String? = null
)

@Serializable
data class Delta(
    val role: String? = null,
    val content: List<ContentDelta>? = null
)

@Serializable
data class ContentDelta(
    val type: String? = null,
    val text: TextDelta? = null
)

@Serializable
data class TextDelta(
    val value: String? = null,
    val annotations: List<Annotation>? = null
)

@Serializable
data class Annotation(
    val type: String? = null,
    val text: String? = null
) 