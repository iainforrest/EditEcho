// RunRequestBody.kt
package com.example.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunRequestBody(
    @SerialName("assistant_id")
    val assistantId: String,
    @SerialName("stream")
    val stream: Boolean = true
)
