package com.example.editecho.network

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.aallam.openai.api.assistant.Assistant
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.assistant.AssistantRole
import com.aallam.openai.api.assistant.AssistantTool
import com.aallam.openai.api.assistant.AssistantToolType
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Client for interacting with OpenAI's Assistants API.
 */
class AssistantApiClient(private val openAI: OpenAI) {
    
    // Assistant ID for the EditEcho assistant
    private val ASSISTANT_ID = "asst_p0phw0bPpJw88hhcEisHkNEQ"
    
    // Maximum number of polling attempts
    private val MAX_POLLING_ATTEMPTS = 30
    
    // Delay between polling attempts (in milliseconds)
    private val POLLING_DELAY = 1000L
    
    init {
        // Validate Assistant ID
        if (ASSISTANT_ID.isBlank() || !ASSISTANT_ID.startsWith("asst_")) {
            Log.e("AssistantApiClient", "Invalid Assistant ID format")
        }
        
        Log.d("AssistantApiClient", "Initialized with Assistant ID: $ASSISTANT_ID")
    }

    /**
     * Processes text with the selected tone using OpenAI's Assistants API.
     *
     * @param text The text to process.
     * @param tone The tone to use for processing ("SMS", "Email", or "Pro").
     * @return The processed text.
     */
    suspend fun processTextWithTone(text: String, tone: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d("AssistantApiClient", "Starting processTextWithTone with tone: $tone and text length: ${text.length}")
            
            // Create a chat completion request
            val request = ChatCompletionRequest(
                model = ModelId("gpt-4"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "You are a helpful assistant that refines text according to the specified tone. " +
                                "For 'SMS', make it casual and concise. " +
                                "For 'Email', make it professional but friendly. " +
                                "For 'Pro', make it highly professional and formal."
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Please refine the following text in a $tone tone: $text"
                    )
                )
            )
            
            // Get the chat completion
            val response = openAI.chatCompletion(request)
            
            // Extract the assistant's message
            val refinedText = response.choices.firstOrNull()?.message?.content ?: "No response generated"
            
            Log.d("AssistantApiClient", "Refined text: $refinedText")
            return@withContext refinedText
            
        } catch (e: Exception) {
            Log.e("AssistantApiClient", "Error processing text", e)
            throw e
        }
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