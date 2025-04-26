// MessagesListResponse.kt
package com.example.editecho.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesListResponse(
    val data: List<MessageResponse>
)
