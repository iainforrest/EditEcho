// MessagesListResponse.kt
package com.example.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessagesListResponse(
    @SerialName("data")
    val data: List<MessageResponse>
)
