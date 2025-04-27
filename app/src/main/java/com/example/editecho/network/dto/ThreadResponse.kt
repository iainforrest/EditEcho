// ThreadResponse.kt
package com.example.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadResponse(
    @SerialName("id")
    val id: String
)
