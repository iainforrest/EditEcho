package com.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunResponse(
    val id: String,
    val status: String,
    @SerialName("thread_id")
    val threadId: String?,
    @SerialName("assistant_id")
    val assistantId: String?,
    @SerialName("created_at")
    val createdAt: Long?,
    val model: String?,
    val usage: Usage? = null,
    @SerialName("last_error")
    val lastError: LastError? = null
) {
    @Serializable
    data class LastError(
        val message: String?
    )
} 