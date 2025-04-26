// ThreadResponse.kt
package com.example.editecho.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThreadResponse(val id: String)
