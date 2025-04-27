package com.example.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WhisperResponse(
    @SerialName("text")
    val text: String
) 