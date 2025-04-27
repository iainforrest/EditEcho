package com.example.editecho.network

import com.example.editecho.network.dto.ChatCompletionRequest
import com.example.editecho.network.dto.ChatMessage
import com.example.editecho.prompt.PromptBuilder
import com.example.editecho.prompt.ToneProfile
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
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun streamReply(tone: ToneProfile, userText: String): Flow<String> = callbackFlow {
        val systemPrompt = PromptBuilder.buildSystemPrompt(tone)
        
        // Select model based on tone
        val model = when (tone) {
            ToneProfile.QUICK, ToneProfile.FRIENDLY -> "gpt-4.1-mini"
            ToneProfile.POLISHED -> "gpt-4.1"
        }
        
        val req = ChatCompletionRequest(
            model = model,
            stream = true,
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", userText)
            ),
            temperature = 0.35,
            topP = 0.95
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
                    if (payload == "[DONE]") continue
                    val resp = json.decodeFromString<ChatCompletionChunk>(payload)
                    resp.choices.firstOrNull()?.delta?.content?.let { trySend(it) }
                }
            }
        }
        awaitClose { }
    }
}

@Serializable
private data class ChatCompletionChunk(
    val choices: List<Choice>
) {
    @Serializable data class Choice(val delta: Delta)
    @Serializable data class Delta(val content: String? = null)
} 