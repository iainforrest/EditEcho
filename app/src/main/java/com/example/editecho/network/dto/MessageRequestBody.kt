// MessageRequestBody.kt
package com.example.editecho.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageRequestBody(
    val role: String = "user",
    val content: String
)
