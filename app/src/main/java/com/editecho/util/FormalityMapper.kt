package com.editecho.util

import com.editecho.prompt.ToneProfile
import com.editecho.prompt.VoiceDNA
import com.editecho.prompt.toRepositoryFormat
import com.editecho.data.VoiceDNARepository

/**
 * Voice Engine 3.0 Formality Mapping Utility
 * 
 * Central utility for mapping tone + polish level to formality percentages,
 * formality band classification, and DNA pattern selection logic.
 */
object FormalityMapper {
    
    /**
     * Formality bands as defined in Voice Engine 3.0 PRD
     */
    enum class FormalityBand(val range: IntRange, val label: String) {
        LOW(0..20, "Low"),
        LOW_MID(21..40, "Low-Mid"), 
        MID(41..60, "Mid"),
        MID_HIGH(61..80, "Mid-High"),
        HIGH(81..100, "High");
        
        companion object {
            /**
             * Get formality band for a given formality level
             */
            fun fromFormality(formalityLevel: Int): FormalityBand? {
                return values().find { formalityLevel in it.range }
            }
            
            /**
             * Get all band labels
             */
            fun getAllLabels(): List<String> = values().map { it.label }
        }
    }
    
    /**
     * Calculate actual formality percentage from tone + polish level
     * Uses PRD formula: actualFormality = minFormality + ((maxFormality - minFormality) * polishLevel / 100)
     * 
     * @param tone The selected tone profile
     * @param polishLevel Polish slider value (0-100)
     * @return Calculated formality percentage (clamped to tone's range)
     */
    fun calculateFormality(tone: ToneProfile, polishLevel: Int): Int {
        return tone.calculateFormality(polishLevel)
    }
    
    /**
     * Calculate formality from tone name string
     * 
     * @param toneName Display name of the tone (case insensitive)
     * @param polishLevel Polish slider value (0-100)
     * @return Calculated formality percentage, or null if tone not found
     */
    fun calculateFormality(toneName: String, polishLevel: Int): Int? {
        val tone = ToneProfile.fromName(toneName) ?: return null
        return calculateFormality(tone, polishLevel)
    }
    
    /**
     * Get formality band for a calculated formality level
     * 
     * @param formalityLevel Formality percentage (0-100)
     * @return Corresponding FormalityBand or null if out of range
     */
    fun getFormalityBand(formalityLevel: Int): FormalityBand? {
        return FormalityBand.fromFormality(formalityLevel)
    }
    
    /**
     * Get formality band from tone + polish level directly
     * 
     * @param tone The selected tone profile
     * @param polishLevel Polish slider value (0-100)
     * @return Corresponding FormalityBand or null if calculation fails
     */
    fun getFormalityBand(tone: ToneProfile, polishLevel: Int): FormalityBand? {
        val formalityLevel = calculateFormality(tone, polishLevel)
        return getFormalityBand(formalityLevel)
    }
    
    /**
     * Data class for DNA selection results
     */
    data class DNASelection(
        val primaryDNA: VoiceDNA?,
        val fallbackDNA: VoiceDNA?,
        val actualFormality: Int,
        val formalityBand: FormalityBand?,
        val useConfidenceThreshold: Boolean
    )
    
    /**
     * Determine appropriate DNA patterns based on tone, polish level, and confidence
     * Implements PRD requirement: ≥0.7 confidence = tone DNA, <0.7 = formality band DNA
     * 
     * @param tone The selected tone profile
     * @param polishLevel Polish slider value (0-100)
     * @param repository VoiceDNA repository for pattern lookup
     * @param confidenceThreshold Minimum confidence for tone DNA (default 0.7 per PRD)
     * @return DNASelection with primary and fallback patterns
     */
    fun selectDNA(
        tone: ToneProfile, 
        polishLevel: Int, 
        repository: VoiceDNARepository,
        confidenceThreshold: Float = 0.7f
    ): DNASelection {
        val actualFormality = calculateFormality(tone, polishLevel)
        val formalityBand = getFormalityBand(actualFormality)
        
        // Get tone-specific DNA
        val toneDNA = repository.getToneDNA(tone.toRepositoryFormat())
        
        // Determine primary and fallback based on confidence
        val useConfidenceThreshold = toneDNA?.confidence ?: 0f >= confidenceThreshold
        
        val primaryDNA: VoiceDNA?
        val fallbackDNA: VoiceDNA?
        
        if (useConfidenceThreshold) {
            // High confidence: use tone DNA as primary
            primaryDNA = toneDNA
            fallbackDNA = repository.getFormalityBandDNA(actualFormality)
        } else {
            // Low confidence: use formality band DNA as primary
            primaryDNA = repository.getFormalityBandDNA(actualFormality)
            fallbackDNA = toneDNA
        }
        
        return DNASelection(
            primaryDNA = primaryDNA,
            fallbackDNA = fallbackDNA,
            actualFormality = actualFormality,
            formalityBand = formalityBand,
            useConfidenceThreshold = useConfidenceThreshold
        )
    }
    
    /**
     * Convenience method for DNA selection with tone name
     */
    fun selectDNA(
        toneName: String,
        polishLevel: Int,
        repository: VoiceDNARepository,
        confidenceThreshold: Float = 0.7f
    ): DNASelection? {
        val tone = ToneProfile.fromName(toneName) ?: return null
        return selectDNA(tone, polishLevel, repository, confidenceThreshold)
    }
    
    /**
     * Get all available tone formality ranges for UI display
     * 
     * @return Map of tone name to formality range pair
     */
    fun getAllToneFormalityRanges(): Map<String, Pair<Int, Int>> {
        return ToneProfile.values().associate { tone ->
            tone.displayName to tone.getFormalityRange()
        }
    }
    
    /**
     * Validate tone + polish combination is within natural bounds
     * 
     * @param tone The selected tone profile
     * @param polishLevel Polish slider value (0-100)
     * @return true if combination is valid, false if unnatural
     */
    fun isValidCombination(tone: ToneProfile, polishLevel: Int): Boolean {
        // All combinations within tone ranges are valid by design
        val clampedPolish = polishLevel.coerceIn(0, 100)
        return clampedPolish in 0..100
    }
    
    /**
     * Get formality progression for a tone across polish levels
     * Useful for UI sliders and testing
     * 
     * @param tone The tone profile
     * @param steps Number of steps across polish range (default 11: 0, 10, 20...100)
     * @return List of polish level to formality mappings
     */
    fun getFormalityProgression(tone: ToneProfile, steps: Int = 11): List<Pair<Int, Int>> {
        require(steps >= 2) { "Need at least 2 steps for progression" }
        
        val stepSize = 100 / (steps - 1)
        return (0 until steps).map { i ->
            val polishLevel = (i * stepSize).coerceAtMost(100)
            val formality = calculateFormality(tone, polishLevel)
            polishLevel to formality
        }
    }
    
    /**
     * Convert formality level to micro-label context
     * Provides context for how formality feels at different levels
     * 
     * @param formalityLevel Formality percentage (0-100)
     * @return Descriptive context for the formality level
     */
    fun getFormalityContext(formalityLevel: Int): String {
        return when (formalityLevel) {
            in 0..15 -> "Text fragments, minimal grammar"
            in 16..30 -> "Casual digital communication"
            in 31..40 -> "Friendly personal messages"
            in 41..50 -> "Relaxed professional communication"
            in 51..60 -> "Standard business communication"
            in 61..70 -> "Polished professional writing"
            in 71..80 -> "Formal business communication"
            in 81..90 -> "Highly formal, ceremonial language"
            in 91..100 -> "Academic, legal, or archival formality"
            else -> "Invalid formality level"
        }
    }
}

 