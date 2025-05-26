package com.editecho.network

import android.util.Log
import com.editecho.prompt.VoiceSettings
import com.editecho.prompt.VoicePromptBuilder
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for handling Claude API completions with Voice DNA integration
 */
@Singleton
class ClaudeCompletionClient @Inject constructor(
    private val api: ClaudeApi,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
) {
    companion object {
        private const val TAG = "ClaudeCompletionClient"
        private const val MODEL = "claude-sonnet-4-20250514"
    }

    /**
     * Complete a text using Claude API with voice settings and Voice DNA patterns
     * 
     * @param voiceSettings The formality and polish levels (0-100)
     * @param userText The raw text to be edited
     * @return The edited text from Claude, or original text if API fails
     */
    suspend fun complete(voiceSettings: VoiceSettings, userText: String): String {
        return try {
            // Build the prompt using VoicePromptBuilder
            val prompt = VoicePromptBuilder.buildPrompt(voiceSettings, userText)
            
            // Log the request for debugging
            Log.d(TAG, "Claude request - Formality: ${voiceSettings.formality}, Polish: ${voiceSettings.polish}")
            Log.d(TAG, "Input text length: ${userText.length}")
            
            // Create Claude request
            val request = ClaudeRequest(
                model = MODEL,
                messages = listOf(
                    ClaudeMessage(
                        role = "user",
                        content = prompt
                    )
                ),
                maxTokens = 4096
            )
            
            // Make the API call
            val response = api.createMessage(request)
            
            // Extract text from the first content block
            val editedText = response.content.firstOrNull()?.text ?: userText
            
            Log.d(TAG, "Claude response received, output length: ${editedText.length}")
            
            editedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Claude API call failed", e)
            // Return original text if API fails
            userText
        }
    }
    
    /**
     * Test function to verify the client works correctly with real API calls
     * This can be called to test the Claude API integration with different voice settings
     */
    suspend fun runApiTest(): String {
        val testResults = StringBuilder()
        
        Log.d(TAG, "=== ClaudeCompletionClient API Test Starting ===")
        testResults.append("=== ClaudeCompletionClient API Test Results ===\n\n")
        
        // Test 1: Casual/Raw (1,1)
        val casualSettings = VoiceSettings(formality = 1, polish = 1)
        val testText1 = "hey mate just wanted to check if you reckon we should prob go with option a or b for the thing we discussed yesterday"
        
        Log.d(TAG, "Test 1: Casual/Raw - Formality 1, Polish 1")
        val result1 = complete(casualSettings, testText1)
        testResults.append("Test 1 - Casual/Raw (1,1):\n")
        testResults.append("Input: $testText1\n")
        testResults.append("Output: $result1\n\n")
        
        // Test 2: Formal/Polished (5,5)
        val formalSettings = VoiceSettings(formality = 5, polish = 5)
        val testText2 = "um so yeah we need to um figure out the logistics for next week and stuff"
        
        Log.d(TAG, "Test 2: Formal/Polished - Formality 5, Polish 5")
        val result2 = complete(formalSettings, testText2)
        testResults.append("Test 2 - Formal/Polished (5,5):\n")
        testResults.append("Input: $testText2\n")
        testResults.append("Output: $result2\n\n")
        
        // Test 3: Balanced (3,3)
        val balancedSettings = VoiceSettings(formality = 3, polish = 3)
        val testText3 = "all good with the meeting. sorry but i think we should prob postpone until next week if thats ok"
        
        Log.d(TAG, "Test 3: Balanced - Formality 3, Polish 3")
        val result3 = complete(balancedSettings, testText3)
        testResults.append("Test 3 - Balanced (3,3):\n")
        testResults.append("Input: $testText3\n")
        testResults.append("Output: $result3\n\n")
        
        Log.d(TAG, "=== ClaudeCompletionClient API Test Complete ===")
        testResults.append("=== Test Complete ===")
        
        val finalResults = testResults.toString()
        Log.d(TAG, finalResults)
        
        return finalResults
    }
    
    /**
     * Quick test function for basic verification
     */
    suspend fun testComplete(): String {
        val testSettings = VoiceSettings(formality = 3, polish = 3)
        val testText = "hey mate just wanted to check if you reckon we should prob go with option a or b for the thing we discussed yesterday"
        
        Log.d(TAG, "=== ClaudeCompletionClient Basic Test ===")
        Log.d(TAG, "Test settings: Formality ${testSettings.formality}/5, Polish ${testSettings.polish}/5")
        Log.d(TAG, "Test input: \"$testText\"")
        
        val result = complete(testSettings, testText)
        
        Log.d(TAG, "Test result: \"$result\"")
        Log.d(TAG, "=== Basic Test Complete ===")
        
        return result
    }
} 