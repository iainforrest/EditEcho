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
            - Keep speaker’s meaning + style. No new content.  
            - OK to reorder for clarity.  
            - If later explicit correction (actually / sorry / I mean / scratch that / no let’s / rather), drop earlier version.  
            - Kill exact dupes, ums/uhs, “so yeah / kinda / sorta / really / just / anyway / you know”.  
            - Merge near-dupes only when no new info.  
            - Len = input –15 % / +10 %.  
            - Caps at sentence starts + “I” (unless Formality 0).  
            - Use commas / periods; ≤1 semicolon per 2 sentences.  
            - If Polish ≥ 4 & ≥3 list items → bullet / line-break them.  
            - Output text only.

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