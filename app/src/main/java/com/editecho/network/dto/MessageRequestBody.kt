// MessageRequestBody.kt
package com.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageRequestBody(
    @SerialName("role")
    val role: String = "user",
    @SerialName("content")
    val content: String
)
