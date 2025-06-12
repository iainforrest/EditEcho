// src/main/java/com/editecho/prompt/ToneProfile.kt
package com.editecho.prompt

/**
 * Voice Engine 3.0 tone profiles with formality ranges and micro-labels
 */
enum class ToneProfile(
    val displayName: String,
    val description: String,
    val minFormality: Int,
    val maxFormality: Int,
    val lowMicroLabel: String,
    val highMicroLabel: String
) {
    CASUAL(
        displayName = "Casual",
        description = "Relaxed, informal social chat",
        minFormality = 10,
        maxFormality = 40,
        lowMicroLabel = "relaxed",
        highMicroLabel = "clear"
    ),
    
    NEUTRAL(
        displayName = "Neutral",
        description = "Plain, factual updates without subjective stance",
        minFormality = 20,
        maxFormality = 70,
        lowMicroLabel = "simple",
        highMicroLabel = "structured"
    ),
    
    INFORMATIVE(
        displayName = "Informative",
        description = "Detailed explanations, step-by-step information",
        minFormality = 25,
        maxFormality = 75,
        lowMicroLabel = "empathetic",
        highMicroLabel = "direct"
    ),
    
    SUPPORTIVE(
        displayName = "Supportive",
        description = "Empathetic and reassuring communication",
        minFormality = 20,
        maxFormality = 60,
        lowMicroLabel = "empathetic",
        highMicroLabel = "reassuring"
    ),
    
    THOUGHTFUL(
        displayName = "Thoughtful",
        description = "Introspective, reflective exploration of ideas",
        minFormality = 30,
        maxFormality = 70,
        lowMicroLabel = "brief",
        highMicroLabel = "structured"
    );

    /**
     * Calculate actual formality level based on polish slider position (0-100)
     */
    fun calculateFormality(polishLevel: Int): Int {
        val clampedPolish = polishLevel.coerceIn(0, 100)
        return minFormality + ((maxFormality - minFormality) * clampedPolish / 100)
    }
    
    /**
     * Get the formality range as a pair
     */
    fun getFormalityRange(): Pair<Int, Int> = Pair(minFormality, maxFormality)
    
    /**
     * Get micro-labels as a pair
     */
    fun getMicroLabels(): Pair<String, String> = Pair(lowMicroLabel, highMicroLabel)

    companion object {
        /**
         * Get ToneProfile by display name (case insensitive)
         */
        fun fromName(name: String): ToneProfile? =
            values().firstOrNull { it.displayName.equals(name, ignoreCase = true) }
        
        /**
         * Get default tone profile
         */
        fun getDefault(): ToneProfile = NEUTRAL
        
        /**
         * Get all available tone names
         */
        fun getAllToneNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * Convert to VoiceDNA repository tone name format
 */
fun ToneProfile.toRepositoryFormat(): String = this.displayName
