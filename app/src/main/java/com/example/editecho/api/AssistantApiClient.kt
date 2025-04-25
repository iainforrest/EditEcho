package com.example.editecho.api

import android.util.Log
import com.example.editecho.prompt.ToneProfile
import com.example.editecho.prompt.PromptBuilder

class AssistantApiClient {
    companion object {
        private const val TAG = "AssistantApiClient"
    }
    
    private fun buildPrompt(text: String, tone: ToneProfile): String {
        val systemPrompt = PromptBuilder.buildSystemPrompt(tone)
        val userPrompt = """
            Tone: ${tone.fullLabel}

            $text
        """.trimIndent()
        
        val fullPrompt = """
            System Prompt:
            $systemPrompt
            
            User Prompt:
            $userPrompt
        """.trimIndent()
        
        // Log the complete prompt
        Log.d(TAG, "Sending prompt to Assistant API:\n$fullPrompt")
        
        return fullPrompt
    }
    
    fun processTextWithTone(text: String, tone: ToneProfile): String {
        val prompt = buildPrompt(text, tone)
        // TODO: Send prompt to API and return response
        return "Processed text with tone: ${tone.displayName}"
    }
} 