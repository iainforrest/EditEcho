package com.editecho.prompt

import com.editecho.data.VoiceDNARepository
import com.editecho.util.FormalityMapper

/**
 * Voice Engine 3.0 Prompt Builder with confidence-based DNA blending
 * 
 * Integrates tone selection, formality mapping, and DNA pattern selection
 * to create contextually appropriate prompts for voice transcription editing.
 */
object VoicePromptBuilder {
    
    /**
     * Builds a Voice Engine 3.0 prompt using tone + polish level with DNA confidence logic
     * 
     * @param tone The selected tone profile
     * @param polishLevel Polish slider value (0-100)
     * @param rawText The raw transcribed text to be edited
     * @param repository VoiceDNA repository for pattern lookup
     * @param confidenceThreshold Minimum confidence for tone DNA (default 0.7 per PRD)
     * @return A formatted prompt string containing appropriate DNA patterns
     */
    fun buildPrompt(
        tone: ToneProfile,
        polishLevel: Int,
        rawText: String,
        repository: VoiceDNARepository,
        confidenceThreshold: Float = 0.7f
    ): String {
        // Use FormalityMapper to get DNA selection
        val dnaSelection = FormalityMapper.selectDNA(tone, polishLevel, repository, confidenceThreshold)
        
        // Calculate actual formality for prompt context
        val actualFormality = FormalityMapper.calculateFormality(tone, polishLevel)
        
        return buildPromptFromDNA(
            tone = tone,
            polishLevel = polishLevel,
            actualFormality = actualFormality,
            dnaSelection = dnaSelection,
            rawText = rawText
        )
    }
    
    /**
     * Builds a Voice Engine 3.0 prompt using tone name string
     * 
     * @param toneName Display name of the tone (e.g., "Casual", "Neutral")
     * @param polishLevel Polish slider value (0-100)
     * @param rawText The raw transcribed text to be edited
     * @param repository VoiceDNA repository for pattern lookup
     * @param confidenceThreshold Minimum confidence for tone DNA (default 0.7 per PRD)
     * @return A formatted prompt string or null if tone name is invalid
     */
    fun buildPrompt(
        toneName: String,
        polishLevel: Int,
        rawText: String,
        repository: VoiceDNARepository,
        confidenceThreshold: Float = 0.7f
    ): String? {
        val tone = ToneProfile.fromName(toneName) ?: return null
        return buildPrompt(tone, polishLevel, rawText, repository, confidenceThreshold)
    }
    
    /**
     * Legacy compatibility: builds prompt using Voice Engine 2.0 VoiceSettings
     * @deprecated Use tone + polish level for Voice Engine 3.0
     */
    @Deprecated("Use tone + polish level for Voice Engine 3.0", ReplaceWith("buildPrompt(tone, polishLevel, rawText, repository)"))
    fun buildPrompt(voiceSettings: VoiceSettings, rawText: String): String {
        return """You are editing a voice transcription. Your task is to transform raw voice-to-text input into clear, coherent text while preserving the speaker's authentic voice.

            CRITICAL RULES:
            - Keep speaker's meaning + style. No new content.  
            - OK to reorder for clarity.  
            - If later explicit correction (actually / sorry / I mean / scratch that / no let's / rather), drop earlier version.  
            - Kill exact dupes, ums/uhs, "so yeah / kinda / sorta / really / just / anyway / you know".  
            - Merge near-dupes only when no new info.  
            - Len = input –15 % / +10 %.  
            - Caps at sentence starts + "I" (unless Formality 0).  
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
     * Internal method to build prompt from DNA selection results
     */
    private fun buildPromptFromDNA(
        tone: ToneProfile,
        polishLevel: Int,
        actualFormality: Int,
        dnaSelection: FormalityMapper.DNASelection,
        rawText: String
    ): String {
        val primaryDNA = dnaSelection.primaryDNA
        val fallbackDNA = dnaSelection.fallbackDNA
        val formalityBand = dnaSelection.formalityBand
        
        return when {
            // High confidence tone DNA available
            primaryDNA != null && dnaSelection.useConfidenceThreshold -> {
                buildHighConfidencePrompt(
                    tone = tone,
                    polishLevel = polishLevel,
                    actualFormality = actualFormality,
                    toneDNA = primaryDNA,
                    formalityBandDNA = fallbackDNA,
                    rawText = rawText
                )
            }
            
            // Low confidence: use formality band DNA with tone intent
            primaryDNA != null && !dnaSelection.useConfidenceThreshold -> {
                buildLowConfidencePrompt(
                    tone = tone,
                    polishLevel = polishLevel,
                    actualFormality = actualFormality,
                    formalityBandDNA = primaryDNA,
                    toneDNA = fallbackDNA,
                    formalityBand = formalityBand,
                    rawText = rawText
                )
            }
            
            // Fallback: no suitable DNA patterns found
            else -> {
                buildFallbackPrompt(
                    tone = tone,
                    polishLevel = polishLevel,
                    actualFormality = actualFormality,
                    rawText = rawText
                )
            }
        }
    }
    
    /**
     * High confidence prompt: Use tone DNA as primary with formality guidance
     */
    private fun buildHighConfidencePrompt(
        tone: ToneProfile,
        polishLevel: Int,
        actualFormality: Int,
        toneDNA: VoiceDNA,
        formalityBandDNA: VoiceDNA?,
        rawText: String
    ): String {
        val formalityGuidance = formalityBandDNA?.let { band ->
            """
            
            FORMALITY BAND GUIDANCE (${band.tone} - ${actualFormality}%):
            ${band.formalityShifts}
            """.trimIndent()
        } ?: ""
        
        return """You are editing a voice transcription. Your task is to transform raw voice-to-text input into clear, coherent text while preserving the speaker's authentic voice.

            CRITICAL RULES:
            - Keep speaker's meaning + style. No new content.  
            - OK to reorder for clarity.  
            - If later explicit correction (actually / sorry / I mean / scratch that / no let's / rather), drop earlier version.  
            - Kill exact dupes, ums/uhs, "so yeah / kinda / sorta / really / just / anyway / you know".  
            - Merge near-dupes only when no new info.  
            - Len = input –15 % / +10 %.  
            - Caps at sentence starts + "I" (unless Formality 0).  
            - Use commas / periods; ≤1 semicolon per 2 sentences.  
            - If Polish ≥ 4 & ≥3 list items → bullet / line-break them.  
            - Output text only.
            IMPORTANT: Don’t use em-dashes. Replace every — with a comma, semicolon, or parentheses.

            TONE DNA PATTERNS (${toneDNA.tone}, confidence ${toneDNA.confidence}):
            
            FORMALITY SHIFTS:
            ${toneDNA.formalityShifts}
            
            POLISH PATTERNS:
            ${toneDNA.polishPatterns}
            
            MUST PRESERVE:
            ${toneDNA.constants}
            
            UNIQUE MARKERS TO KEEP:
            ${toneDNA.voiceMarkers}
            
            AVOID:
            ${toneDNA.antiPatterns}$formalityGuidance

            TARGET: ${tone.displayName} tone at ${actualFormality}% formality, ${polishLevel}% polish
            
            Edit this text:
            $rawText

            Return only the edited text, nothing else.
        """.trimIndent()
    }
    
    /**
     * Low confidence prompt: Use formality band DNA as primary with tone intent
     */
    private fun buildLowConfidencePrompt(
        tone: ToneProfile,
        polishLevel: Int,
        actualFormality: Int,
        formalityBandDNA: VoiceDNA,
        toneDNA: VoiceDNA?,
        formalityBand: FormalityMapper.FormalityBand?,
        rawText: String
    ): String {
        val toneIntent = toneDNA?.let { tDNA ->
            """
            
            TONE INTENT (${tDNA.tone}, low confidence ${tDNA.confidence}):
            Apply ${tone.displayName} communication intent using the above formality patterns.
            Voice markers to incorporate: ${tDNA.voiceMarkers}
            """.trimIndent()
        } ?: """
        
        TONE INTENT: Apply ${tone.displayName} communication intent using the above formality patterns.
        """.trimIndent()
        
        return """You are editing a voice transcription. Your task is to transform raw voice-to-text input into clear, coherent text while preserving the speaker's authentic voice.

            CRITICAL RULES:
            - Keep speaker's meaning + style. No new content.  
            - OK to reorder for clarity.  
            - If later explicit correction (actually / sorry / I mean / scratch that / no let's / rather), drop earlier version.  
            - Kill exact dupes, ums/uhs, "so yeah / kinda / sorta / really / just / anyway / you know".  
            - Merge near-dupes only when no new info.  
            - Len = input –15 % / +10 %.  
            - Caps at sentence starts + "I" (unless Formality 0).  
            - Use commas / periods; ≤1 semicolon per 2 sentences.  
            - If Polish ≥ 4 & ≥3 list items → bullet / line-break them.  
            - Output text only.
             IMPORTANT: Don’t use em-dashes. Replace every — with a comma, semicolon, or parentheses.

            FORMALITY BAND PATTERNS (${formalityBandDNA.tone} - ${actualFormality}%, confidence ${formalityBandDNA.confidence}):
            
            FORMALITY SHIFTS:
            ${formalityBandDNA.formalityShifts}
            
            POLISH PATTERNS:
            ${formalityBandDNA.polishPatterns}
            
            MUST PRESERVE:
            ${formalityBandDNA.constants}
            
            VOICE MARKERS:
            ${formalityBandDNA.voiceMarkers}
            
            AVOID:
            ${formalityBandDNA.antiPatterns}$toneIntent

            TARGET: ${tone.displayName} intent at ${formalityBand?.label ?: "Unknown"} formality (${actualFormality}%), ${polishLevel}% polish
            
            Edit this text:
            $rawText

            Return only the edited text, nothing else.
        """.trimIndent()
    }
    
    /**
     * Fallback prompt: No DNA patterns available, use tone description and formality context
     */
    private fun buildFallbackPrompt(
        tone: ToneProfile,
        polishLevel: Int,
        actualFormality: Int,
        rawText: String
    ): String {
        val formalityContext = FormalityMapper.getFormalityContext(actualFormality)
        
        return """You are editing a voice transcription. Your task is to transform raw voice-to-text input into clear, coherent text while preserving the speaker's authentic voice.

            CRITICAL RULES:
            - Keep speaker's meaning + style. No new content.  
            - OK to reorder for clarity.  
            - If later explicit correction (actually / sorry / I mean / scratch that / no let's / rather), drop earlier version.  
            - Kill exact dupes, ums/uhs, "so yeah / kinda / sorta / really / just / anyway / you know".  
            - Merge near-dupes only when no new info.  
            - Len = input –15 % / +10 %.  
            - Caps at sentence starts + "I" (unless Formality 0).  
            - Use commas / periods; ≤1 semicolon per 2 sentences.  
            - If Polish ≥ 4 & ≥3 list items → bullet / line-break them.  
            - Output text only.
             IMPORTANT: Don’t use em-dashes. Replace every — with a comma, semicolon, or parentheses.

            TONE GUIDANCE: ${tone.displayName} - ${tone.description}
            FORMALITY GUIDANCE: ${formalityContext}
            POLISH LEVEL: ${polishLevel}% (${tone.lowMicroLabel} → ${tone.highMicroLabel})

            TARGET: Transform text to match ${tone.displayName} tone at ${actualFormality}% formality, ${polishLevel}% polish
            
            Edit this text:
            $rawText

            Return only the edited text, nothing else.
        """.trimIndent()
    }
    
    /**
     * Get voice engine settings for a given tone and polish combination
     */
    fun getVoiceEngine3Settings(tone: ToneProfile, polishLevel: Int): VoiceEngine3Settings {
        return VoiceEngine3Settings(
            selectedTone = tone.displayName,
            polishLevel = polishLevel
        )
    }
    
    /**
     * Test function for Voice Engine 3.0 prompt building
     */
    fun testVoiceEngine3PromptBuilder(repository: VoiceDNARepository): String {
        val testTone = ToneProfile.CASUAL
        val testPolishLevel = 50
        val testText = "hey mate just wanted to check if you reckon we should prob go with option a"
        
        return buildPrompt(testTone, testPolishLevel, testText, repository)
    }
}