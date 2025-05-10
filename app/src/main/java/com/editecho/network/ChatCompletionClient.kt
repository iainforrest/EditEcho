package com.editecho.network

import com.editecho.network.dto.ChatCompletionRequest
import com.editecho.network.dto.ChatMessage
import com.editecho.prompt.PromptBuilder
import com.editecho.prompt.ToneProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import java.io.IOException
import java.io.BufferedReader
import java.io.Closeable
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.BufferedSource
import android.util.Log

class ChatCompletionClient(
    private val api: OpenAiChatApi,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true   
    }
) {
    fun streamReply(tone: ToneProfile, userText: String): Flow<String> = callbackFlow {
        val systemPrompt = PromptBuilder.buildSystemPrompt(tone)
        
        // Select model based on tone
        val model = selectModel(tone)
        
        val req = ChatCompletionRequest(
            model = model,
            stream = true,
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", PromptBuilder.wrapUserInput(userText))
            ),
            temperature = 0.25,
            topP = 0.9
        )
        
        // Log the request for debugging
        Log.d("ChatCompletionClient", "Request: ${json.encodeToString(ChatCompletionRequest.serializer(), req)}")
        
        var attempts = 0
        val responseBody = run {
            var lastResponse: ResponseBody? = null
            while (true) {
                try {
                    lastResponse = api.createCompletion(req)
                    break
                } catch (e: IOException) {
                    if (++attempts >= 3) throw e
                    // exponential back-off: wait longer each retry
                    delay(attempts * 1_000L)
                }
            }
            lastResponse ?: throw IOException("Failed to get response after $attempts attempts")
        }
        
        responseBody.source().use { source ->
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data: ")) {
                    val payload = line.removePrefix("data: ").trim()
                    if (payload == "[DONE]") {
                        channel.close()    // explicitly close the Flow's channel
                        break              // exit the loop
                    }
                    val resp = json.decodeFromString<ChatCompletionChunk>(payload)
                    resp.choices.firstOrNull()?.delta?.content?.let { trySend(it) }
                }
            }
        }
        awaitClose { /* no-op */ }
    }

    private fun selectModel(tone: ToneProfile): String = when (tone) {
        ToneProfile.FRIENDLY, ToneProfile.DIRECT -> "gpt-4.1-mini"
        ToneProfile.ENGAGED, ToneProfile.REFLECTIVE -> "gpt-4.1"
    }
}

@Serializable
private data class ChatCompletionChunk(
    val choices: List<Choice>
) {
    @Serializable data class Choice(val delta: Delta)
    @Serializable data class Delta(val content: String? = null)
} 