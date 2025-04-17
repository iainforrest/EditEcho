package com.example.editecho.network

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.editecho.prompt.PromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Client for interacting with OpenAI's ChatGPT API.
 */
class GptApiClient(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    // API key loaded from BuildConfig
    private val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY

    /**
     * Sends transcribed text to OpenAI's ChatGPT API for refinement.
     *
     * @param transcribedText The text transcribed by Whisper API.
     * @param tone The tone to use for refinement ("SMS", "Email", or "Pro").
     * @param onSuccess Callback function to handle the refined text.
     */
    suspend fun refineText(transcribedText: String, tone: String, onSuccess: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Generate system prompt based on tone
                val systemPrompt = PromptBuilder.buildSystemPrompt(tone)
                
                // Build request body
                val requestBody = buildRequestBody(systemPrompt, transcribedText)
                
                // Create request
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody(jsonMediaType))
                    .build()
                
                Log.d("GptApiClient", "Sending request to OpenAI API")
                
                // Execute request
                client.newCall(request).execute().use { response ->
                    Log.d("GptApiClient", "Received response with code: ${response.code}")
                    
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Log.e("GptApiClient", "API call failed: ${response.code} - $errorBody")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Refinement failed, please try again.", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext
                    }
                    
                    // Parse response
                    val responseBody = response.body?.string() ?: throw IOException("Empty response body")
                    Log.d("GptApiClient", "Received response body: $responseBody")
                    
                    val refinedText = parseResponse(responseBody)
                    Log.d("GptApiClient", "Refined text: $refinedText")
                    
                    // Copy to clipboard
                    withContext(Dispatchers.Main) {
                        copyToClipboard(refinedText)
                        onSuccess(refinedText)
                    }
                }
            } catch (e: Exception) {
                Log.e("GptApiClient", "Error in refineText: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Refinement failed, please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * Builds the request body for the ChatGPT API.
     *
     * @param systemPrompt The system prompt to guide the model's behavior.
     * @param userContent The user's input text.
     * @return The JSON request body as a string.
     */
    private fun buildRequestBody(systemPrompt: String, userContent: String): String {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", userContent)
            })
        }
        
        return JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messages)
            put("max_tokens", 500)
            put("temperature", 0.7)
        }.toString()
    }
    
    /**
     * Parses the response from the ChatGPT API.
     *
     * @param responseBody The response body as a string.
     * @return The refined text.
     */
    private fun parseResponse(responseBody: String): String {
        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        return message.getString("content")
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
} 