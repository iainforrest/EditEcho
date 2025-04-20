package com.example.editecho.network

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import kotlin.time.Duration.Companion.seconds

object OpenAIProvider {
    val client: OpenAI by lazy {
        OpenAI(
            token = com.example.editecho.BuildConfig.OPENAI_API_KEY,
            timeout = Timeout(socket = 60.seconds)
        )
    }
} 