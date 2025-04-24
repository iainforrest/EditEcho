package com.example.editecho.network

import com.aallam.openai.client.OpenAI

object OpenAIProvider {
    val client: OpenAI by lazy {
        OpenAI(
            token = com.example.editecho.BuildConfig.OPENAI_API_KEY
        )
    }
} 