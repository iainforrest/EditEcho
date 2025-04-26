// AssistantApiClient.kt
package com.example.editecho.network

import android.util.Log
import com.example.editecho.network.dto.MessageRequestBody
import com.example.editecho.network.dto.RunRequestBody
import com.example.editecho.network.dto.ThreadResponse
import com.example.editecho.network.dto.RunResponse
import com.example.editecho.network.dto.StreamEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean

class AssistantApiClient(
    private val api: OpenAiAssistantsApi = OpenAiRetrofit.api,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
) {

    companion object {
        private const val TAG = "AssistantApiClient"
        private const val ASSISTANT_ID = "asst_Csg7UseRhcHiKTRN0wx8Q4t4"
    }

    /**
     * Full Edit-Echo flow with streaming:
     * 1) create thread, 2) send user message, 3) create run with Retrofit,
     * 4) open SSE GET to /events, 5) process SSE events, 6) return final text when complete.
     */
    suspend fun startRunStreaming(
        tone: String,
        rawText: String,
        onToken: (String) -> Unit,
        onDone: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Log.d(TAG, "Starting streaming process with tone: $tone")
        
        try {
            // 1️⃣ create thread
            val threadId: String = api.createThread().id
            Log.d(TAG, "Created thread: $threadId")

            // 2️⃣ add user message
            api.addMessage(
                threadId,
                MessageRequestBody(content = "Tone: $tone\n\n$rawText")
            )
            Log.d(TAG, "Added message to thread")

            // 3️⃣ create run with Retrofit
            val run = api.createRun(threadId, RunRequestBody(assistantId = ASSISTANT_ID))
            Log.d(TAG, "Created run: ${run.id}")

            // 4️⃣ open SSE GET to /events
            val sseRequest = Request.Builder()
                .url("https://api.openai.com/v1/threads/$threadId/runs/${run.id}/events")
                .addHeader("Authorization", "Bearer ${com.example.editecho.BuildConfig.OPENAI_API_KEY}")
                .addHeader("OpenAI-Beta", "assistants=v2")
                .build()

            // 5️⃣ process SSE events
            val accumulatedText = StringBuilder()
            val isCompleted = AtomicBoolean(false)
            
            val eventSource = EventSources.createFactory(okHttpClient)
                .newEventSource(sseRequest, object : EventSourceListener() {
                    override fun onEvent(
                        eventSource: EventSource,
                        id: String?,
                        type: String?,
                        data: String
                    ) {
                        try {
                            val event = StreamEvent(
                                event = type ?: "",
                                data = data
                            )
                            
                            val eventData = event.parseData()
                            if (eventData == null) {
                                Log.e(TAG, "Failed to parse event data: $data")
                                return
                            }
                            
                            when (event.event) {
                                "thread.message.delta" -> {
                                    val delta = eventData.delta
                                    if (delta?.role == "assistant") {
                                        val contentDeltas = delta.content ?: return
                                        for (contentDelta in contentDeltas) {
                                            if (contentDelta.type == "text") {
                                                val textValue = contentDelta.text?.value
                                                if (!textValue.isNullOrEmpty()) {
                                                    accumulatedText.append(textValue)
                                                    onToken(textValue)
                                                }
                                            }
                                        }
                                    }
                                }
                                "thread.message.completed" -> {
                                    if (!isCompleted.getAndSet(true)) {
                                        val finalText = accumulatedText.toString()
                                        Log.d(TAG, "Stream completed with text: $finalText")
                                        onDone(finalText)
                                        eventSource.cancel()
                                    }
                                }
                                "thread.run.completed" -> {
                                    Log.d(TAG, "Run completed")
                                }
                                "thread.run.failed" -> {
                                    val errorMsg = "Run failed: ${eventData.delta?.content?.firstOrNull()?.text?.value ?: "Unknown error"}"
                                    Log.e(TAG, errorMsg)
                                    onError(Exception(errorMsg))
                                    eventSource.cancel()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing event", e)
                            onError(e)
                            eventSource.cancel()
                        }
                    }
                    
                    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                        Log.e(TAG, "EventSource failure", t)
                        onError(t ?: Exception("Unknown error"))
                    }
                })
                
            // Wait for completion or error
            withContext(Dispatchers.IO) {
                while (!isCompleted.get()) {
                    delay(100)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in streaming process", e)
            onError(e)
        }
    }
}
