package com.editecho.prompt

/**
 * Builder for creating voice-aware prompts using Voice DNA and voice settings
 */
object VoicePromptBuilder {
    
    /**
     * Builds a prompt for text editing using voice settings and Voice DNA patterns
     * 
     * @param voiceSettings The formality and polish levels (0-100)
     * @param rawText The raw transcribed text to be edited
     * @return A formatted prompt string for the AI containing all Voice DNA patterns
     */
    fun buildPrompt(voiceSettings: VoiceSettings, rawText: String): String {
        return """You are editing a voice transcription. Your task is to transform raw voice-to-text input into clear, coherent text while preserving the speaker's authentic voice.

            CRITICAL RULES:
            - Output ONLY the edited text - no commentary, no additions
            - Edit ONLY what was spoken - do not add new sentences or ideas
            - Do not add greetings, sign-offs, or names unless they were in the original
            - Remove filler words (um, uh) and fix transcription errors
            - Preserve the speaker's intent and meaning
            - Output should be roughly the same length as input

            The speaker's patterns are:

            FORMALITY PATTERNS (apply at level ${voiceSettings.formality}/100):
            ${IainVoiceDNA.voiceDNA.formalityShifts}

            POLISH PATTERNS (apply at level ${voiceSettings.polish}/100):
            ${IainVoiceDNA.voiceDNA.polishPatterns}

            MUST PRESERVE:
            ${IainVoiceDNA.voiceDNA.constants}

            UNIQUE MARKERS TO KEEP:
            ${IainVoiceDNA.voiceDNA.voiceMarkers}

            At formality ${voiceSettings.formality}/100 and polish ${voiceSettings.polish}/100, edit this text:
            $rawText

            Return only the edited text, nothing else.
        """.trimIndent()
    }
    
    /**
     * Test function to verify the prompt builder works correctly
     */
    fun testPromptBuilder(): String {
        val testSettings = VoiceSettings(formality = 50, polish = 50)
        val testText = "hey mate just wanted to check if you reckon we should prob go with option a"
        
        return buildPrompt(testSettings, testText)
    }
}