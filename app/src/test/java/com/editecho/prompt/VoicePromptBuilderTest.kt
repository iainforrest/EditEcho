package com.editecho.prompt

import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for VoicePromptBuilder with 0-100 range values
 */
class VoicePromptBuilderTest {

    @Test
    fun testPromptBuilderWithMinimumValues() {
        val settings = VoiceSettings(formality = 0, polish = 0)
        val testText = "hey mate just wanted to check if you reckon we should prob go with option a"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertTrue("Prompt should contain formality level 0/100", prompt.contains("0/100"))
        assertTrue("Prompt should contain the test text", prompt.contains(testText))
        assertTrue("Prompt should contain Voice DNA patterns", prompt.contains("FORMALITY PATTERNS"))
    }

    @Test
    fun testPromptBuilderWithMiddleValues() {
        val settings = VoiceSettings(formality = 50, polish = 50)
        val testText = "testing middle range values"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertTrue("Prompt should contain formality level 50/100", prompt.contains("50/100"))
        assertTrue("Prompt should contain the test text", prompt.contains(testText))
    }

    @Test
    fun testPromptBuilderWithMaximumValues() {
        val settings = VoiceSettings(formality = 100, polish = 100)
        val testText = "formal and highly polished message"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertTrue("Prompt should contain formality level 100/100", prompt.contains("100/100"))
        assertTrue("Prompt should contain the test text", prompt.contains(testText))
    }

    @Test
    fun testPromptBuilderWithAsymmetricValues() {
        val settings = VoiceSettings(formality = 25, polish = 75)
        val testText = "asymmetric formality and polish levels"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertTrue("Prompt should contain formality level 25/100", prompt.contains("25/100"))
        assertTrue("Prompt should contain polish level 75/100", prompt.contains("75/100"))
    }

    @Test
    fun testDefaultVoiceSettings() {
        val defaultSettings = VoiceSettings(formality = 50, polish = 50)
        
        assertEquals("Default formality should be 50", 50, defaultSettings.formality)
        assertEquals("Default polish should be 50", 50, defaultSettings.polish)
    }
} 