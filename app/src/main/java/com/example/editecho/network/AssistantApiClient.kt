package com.example.editecho.network

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.editecho.BuildConfig

/**
 * Client for interacting with OpenAI's Assistants API.
 */
class AssistantApiClient(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    // API key loaded from BuildConfig
    private val apiKey = BuildConfig.OPENAI_API_KEY
    
    // Assistant ID for the EditEcho assistant
    private val ASSISTANT_ID = "asst_p0phw0bPpJw88hhcEisHkNEQ"
    
    // Maximum number of polling attempts
    private val MAX_POLLING_ATTEMPTS = 30
    
    // Delay between polling attempts (in milliseconds)
    private val POLLING_DELAY = 1000L
    
    init {
        // Validate API key and Assistant ID
        if (apiKey.isBlank() || !apiKey.startsWith("sk-")) {
            Log.e("AssistantApiClient", "Invalid OpenAI API key format")
        }
        
        if (ASSISTANT_ID.isBlank() || !ASSISTANT_ID.startsWith("asst_")) {
            Log.e("AssistantApiClient", "Invalid Assistant ID format")
        }
        
        Log.d("AssistantApiClient", "Initialized with API key: ${apiKey.take(10)}... and Assistant ID: $ASSISTANT_ID")
    }

    /**
     * Refines text using OpenAI's Assistants API.
     *
     * @param transcribedText The text transcribed by Whisper API.
     * @param tone The tone to use for refinement ("SMS", "Email", or "Pro").
     * @param onSuccess Callback function to handle the refined text.
     */
    suspend fun refineText(transcribedText: String, tone: String, onSuccess: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("AssistantApiClient", "Starting refineText with tone: $tone and text length: ${transcribedText.length}")
                
                // Create a new thread
                val threadId = createThread()
                Log.d("AssistantApiClient", "Created thread with ID: $threadId")
                
                // Send a message to the thread
                val messageId = sendMessage(threadId, tone, transcribedText)
                Log.d("AssistantApiClient", "Sent message with ID: $messageId")
                
                // Create a run
                val runId = createRun(threadId)
                Log.d("AssistantApiClient", "Created run with ID: $runId")
                
                // Poll for run completion
                val runStatus = pollRunStatus(threadId, runId)
                Log.d("AssistantApiClient", "Run status: $runStatus")
                
                if (runStatus == "completed") {
                    // Get the assistant's response
                    val refinedText = getAssistantResponse(threadId)
                    Log.d("AssistantApiClient", "Refined text: $refinedText")
                    
                    // Copy to clipboard and update UI
                    withContext(Dispatchers.Main) {
                        copyToClipboard(refinedText)
                        onSuccess(refinedText)
                    }
                } else {
                    // Handle run failure
                    Log.e("AssistantApiClient", "Run failed with status: $runStatus")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Something went wrong while refining your message. Status: $runStatus", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AssistantApiClient", "Error in refineText: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * Creates a new thread.
     *
     * @return The thread ID.
     */
    private fun createThread(): String {
        Log.d("AssistantApiClient", "Creating thread...")
        
        // Create an empty JSONObject for the request body
        val requestBody = JSONObject().toString().toRequestBody(jsonMediaType)
        
        val request = Request.Builder()
            .url("https://api.openai.com/v1/threads")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("OpenAI-Beta", "assistants=v2")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        
        client.newCall(request).execute().use { response ->
            // Store response body in a variable to avoid consuming it twice
            val responseBodyString = response.body?.string() ?: throw IOException("Empty response body")
            
            if (!response.isSuccessful) {
                Log.e("AssistantApiClient", "Failed to create thread: ${response.code} - $responseBodyString")
                throw IOException("Failed to create thread: ${response.code} - $responseBodyString")
            }
            
            Log.d("AssistantApiClient", "Thread creation response: $responseBodyString")
            val jsonResponse = JSONObject(responseBodyString)
            return jsonResponse.getString("id")
        }
    }
    
    /**
     * Sends a message to a thread.
     *
     * @param threadId The thread ID.
     * @param tone The tone to use for refinement.
     * @param transcribedText The transcribed text.
     * @return The message ID.
     */
    private fun sendMessage(threadId: String, tone: String, transcribedText: String): String {
        try {
            Log.d("AssistantApiClient", "Sending message to thread: $threadId")
            
            // Map the tone to a more specific description
            val toneDescription = when (tone) {
                "SMS" -> "SMS (short, informal text message)"
                "Email" -> "Casual email (friendly, conversational email style)"
                "Pro" -> "Professional email (formal business email with proper structure)"
                else -> tone
            }
            
            val content = "Tone: $toneDescription\n\n$transcribedText"
            Log.d("AssistantApiClient", "Message content: $content")
            
            val requestBody = JSONObject().apply {
                put("role", "user")
                put("content", content)
            }.toString()
            
            val request = Request.Builder()
                .url("https://api.openai.com/v1/threads/$threadId/messages")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("OpenAI-Beta", "assistants=v2")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody(jsonMediaType))
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e("AssistantApiClient", "Failed to send message: ${response.code} - $errorBody")
                    throw IOException("Failed to send message: ${response.code} - $errorBody")
                }
                
                val responseBody = response.body?.string() ?: throw IOException("Empty response body")
                Log.d("AssistantApiClient", "Message sent successfully: $responseBody")
                val jsonResponse = JSONObject(responseBody)
                return jsonResponse.getString("id")
            }
        } catch (e: Exception) {
            Log.e("AssistantApiClient", "Exception in sendMessage: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Creates a run for a thread.
     *
     * @param threadId The thread ID.
     * @return The run ID.
     */
    private fun createRun(threadId: String): String {
        val requestBody = JSONObject().apply {
            put("assistant_id", ASSISTANT_ID)
        }.toString()
        
        val request = Request.Builder()
            .url("https://api.openai.com/v1/threads/$threadId/runs")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("OpenAI-Beta", "assistants=v2")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody(jsonMediaType))
            .build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e("AssistantApiClient", "Failed to create run: ${response.code} - $errorBody")
                throw IOException("Failed to create run: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val jsonResponse = JSONObject(responseBody)
            return jsonResponse.getString("id")
        }
    }
    
    /**
     * Polls for run status until completion or failure.
     *
     * @param threadId The thread ID.
     * @param runId The run ID.
     * @return The final run status.
     */
    private suspend fun pollRunStatus(threadId: String, runId: String): String {
        var attempts = 0
        var status = "queued"
        
        while (attempts < MAX_POLLING_ATTEMPTS) {
            val request = Request.Builder()
                .url("https://api.openai.com/v1/threads/$threadId/runs/$runId")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("OpenAI-Beta", "assistants=v2")
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e("AssistantApiClient", "Failed to get run status: ${response.code} - $errorBody")
                    throw IOException("Failed to get run status: ${response.code}")
                }
                
                val responseBody = response.body?.string() ?: throw IOException("Empty response body")
                val jsonResponse = JSONObject(responseBody)
                status = jsonResponse.getString("status")
                
                Log.d("AssistantApiClient", "Run status: $status (attempt ${attempts + 1})")
                
                if (status == "completed" || status == "failed" || status == "cancelled" || status == "expired") {
                    return status
                }
            }
            
            attempts++
            delay(POLLING_DELAY)
        }
        
        return status
    }
    
    /**
     * Gets the assistant's response from a thread.
     *
     * @param threadId The thread ID.
     * @return The assistant's response.
     */
    private fun getAssistantResponse(threadId: String): String {
        val request = Request.Builder()
            .url("https://api.openai.com/v1/threads/$threadId/messages")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("OpenAI-Beta", "assistants=v2")
            .get()
            .build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e("AssistantApiClient", "Failed to get messages: ${response.code} - $errorBody")
                throw IOException("Failed to get messages: ${response.code}")
            }
            
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val jsonResponse = JSONObject(responseBody)
            val data = jsonResponse.getJSONArray("data")
            
            // Get the first (most recent) message
            val firstMessage = data.getJSONObject(0)
            val content = firstMessage.getJSONArray("content")
            
            // Get the text from the first content item
            val firstContent = content.getJSONObject(0)
            val text = firstContent.getJSONObject("text")
            return text.getString("value")
        }
    }
    
    /**
     * Copies text to the clipboard.
     *
     * @param text The text to copy.
     */
    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Refined Text", text)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Processes text with a specific tone using the Assistants API.
     *
     * @param text The text to process.
     * @param tone The tone to apply.
     * @return The processed text.
     */
    suspend fun processTextWithTone(text: String, tone: String): String {
        var result = ""
        refineText(text, tone) { refinedText ->
            result = refinedText
        }
        return result
    }
} 