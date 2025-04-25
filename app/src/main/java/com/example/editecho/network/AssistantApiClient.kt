package com.example.editecho.network

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.aallam.openai.api.assistant.Assistant
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.assistant.Thread
import com.aallam.openai.api.assistant.ThreadId
import com.aallam.openai.api.assistant.ThreadRequest
import com.aallam.openai.api.assistant.ThreadMessage
import com.aallam.openai.api.assistant.ThreadMessageRequest
import com.aallam.openai.api.assistant.ThreadRun
import com.aallam.openai.api.assistant.ThreadRunRequest
import com.aallam.openai.api.assistant.ThreadRunStatus
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Client for interacting with OpenAI's Assistants API v2.
 */
class AssistantApiClient(private val openAI: OpenAI) {
    
    companion object {
        private const val TAG = "AssistantApiClient"
        private const val ASSISTANT_ID = "asst_Csg7UseRhcHiKTRN0wx8Q4t4"
        private const val MAX_POLLING_ATTEMPTS = 30
        private const val POLLING_DELAY_MS = 1000L
    }
    
    init {
        // Validate Assistant ID
        if (ASSISTANT_ID.isBlank() || !ASSISTANT_ID.startsWith("asst_")) {
            Log.e(TAG, "Invalid Assistant ID format")
        }
        
        Log.d(TAG, "Initialized with Assistant ID: $ASSISTANT_ID")
    }

    /**
     * Processes text with the selected tone using OpenAI's Assistants API v2.
     *
     * @param text The text to process.
     * @param tone The tone to use for processing (e.g., "Quick Message", "Friendly Reply", "Clear and Polished").
     * @return The processed text.
     */
    suspend fun processTextWithTone(text: String, tone: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting processTextWithTone with tone: $tone and text length: ${text.length}")
            
            // Format the message content with tone prefix
            val messageContent = """
                Tone: $tone
                
                $text
            """.trimIndent()
            
            // Log the message content
            Log.d(TAG, "Message content:\n$messageContent")
            
            // Step 1: Create a thread
            val thread = createThread()
            val threadId = thread.id
            Log.d(TAG, "Created thread with ID: $threadId")
            
            // Step 2: Add the user message to the thread
            val message = addMessageToThread(threadId, messageContent)
            val messageId = message.id
            Log.d(TAG, "Added message to thread with ID: $messageId")
            
            // Step 3: Create a run with the assistant
            val run = createRun(threadId)
            val runId = run.id
            Log.d(TAG, "Created run with ID: $runId")
            
            // Step 4: Poll until the run completes
            val completedRun = pollRunCompletion(threadId, runId)
            Log.d(TAG, "Run completed with status: ${completedRun.status}")
            
            // Step 5: Get the assistant's response
            val assistantMessage = getAssistantResponse(threadId)
            val refinedText = assistantMessage.content.firstOrNull()?.text?.value ?: ""
            
            Log.d(TAG, "Refined text: $refinedText")
            return@withContext refinedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing text", e)
            Log.e(TAG, "Error details: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
    
    /**
     * Creates a new thread.
     */
    private suspend fun createThread(): Thread {
        val request = ThreadRequest()
        val response = openAI.thread(request)
        Log.d(TAG, "Thread creation response: ${Json { prettyPrint = true }.encodeToString(Thread.serializer(), response)}")
        return response
    }
    
    /**
     * Adds a message to a thread.
     */
    private suspend fun addMessageToThread(threadId: String, content: String): ThreadMessage {
        val request = ThreadMessageRequest(
            role = "user",
            content = content
        )
        val response = openAI.threadMessage(threadId, request)
        Log.d(TAG, "Message creation response: ${Json { prettyPrint = true }.encodeToString(ThreadMessage.serializer(), response)}")
        return response
    }
    
    /**
     * Creates a run with the assistant.
     */
    private suspend fun createRun(threadId: String): ThreadRun {
        val request = ThreadRunRequest(
            assistantId = AssistantId(ASSISTANT_ID)
        )
        val response = openAI.threadRun(threadId, request)
        Log.d(TAG, "Run creation response: ${Json { prettyPrint = true }.encodeToString(ThreadRun.serializer(), response)}")
        return response
    }
    
    /**
     * Polls until the run completes.
     */
    private suspend fun pollRunCompletion(threadId: String, runId: String): ThreadRun {
        var attempts = 0
        var run: ThreadRun? = null
        
        while (attempts < MAX_POLLING_ATTEMPTS) {
            val response = openAI.threadRun(threadId, runId)
            run = response
            
            Log.d(TAG, "Run status: ${run.status}, attempt: ${attempts + 1}")
            
            when (run.status) {
                ThreadRunStatus.COMPLETED -> {
                    Log.d(TAG, "Run completed successfully")
                    break
                }
                ThreadRunStatus.FAILED -> {
                    Log.e(TAG, "Run failed: ${run.lastError?.message}")
                    throw Exception("Run failed: ${run.lastError?.message}")
                }
                ThreadRunStatus.CANCELLED -> {
                    Log.e(TAG, "Run was cancelled")
                    throw Exception("Run was cancelled")
                }
                ThreadRunStatus.EXPIRED -> {
                    Log.e(TAG, "Run expired")
                    throw Exception("Run expired")
                }
                else -> {
                    // Still processing, wait and try again
                    delay(POLLING_DELAY_MS)
                    attempts++
                }
            }
        }
        
        if (attempts >= MAX_POLLING_ATTEMPTS) {
            Log.e(TAG, "Run polling timed out after $MAX_POLLING_ATTEMPTS attempts")
            throw Exception("Run polling timed out after $MAX_POLLING_ATTEMPTS attempts")
        }
        
        return run ?: throw Exception("Run is null")
    }
    
    /**
     * Gets the assistant's response from the thread.
     */
    private suspend fun getAssistantResponse(threadId: String): ThreadMessage {
        val response = openAI.threadMessages(threadId)
        Log.d(TAG, "Thread messages response: ${Json { prettyPrint = true }.encodeToString(List.serializer(ThreadMessage.serializer()), response)}")
        
        // Find the most recent assistant message
        val assistantMessage = response.firstOrNull { it.role == "assistant" }
            ?: throw Exception("No assistant message found in thread")
            
        return assistantMessage
    }
    
    /**
     * Copies text to the clipboard.
     */
    private fun copyToClipboard(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Refined Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }
} 