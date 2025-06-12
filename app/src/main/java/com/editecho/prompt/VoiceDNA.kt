package com.editecho.prompt

/**
 * Data class representing the core components of a person's voice DNA for Voice Engine 3.0
 */
data class VoiceDNA(
    val tone: String,
    val confidence: Float,
    val userFormalityRange: List<Int>,
    val theoreticalRange: List<Int>,
    val formalityShifts: String,
    val polishPatterns: String,
    val constants: String,
    val voiceMarkers: String,
    val antiPatterns: String,
    val sourceExampleIds: List<Int>
)

/**
 * Container for all Voice DNA patterns loaded from JSON
 */
data class VoiceDNACollection(
    val toneSpecificDNA: List<VoiceDNA>,
    val formalityBandDNA: List<VoiceDNA>
) {
    /**
     * Get tone-specific DNA by tone name
     */
    fun getToneDNA(tone: String): VoiceDNA? {
        return toneSpecificDNA.find { it.tone.equals(tone, ignoreCase = true) }
    }
    
    /**
     * Get formality band DNA by formality level
     */
    fun getFormalityBandDNA(formalityLevel: Int): VoiceDNA? {
        return when(formalityLevel) {
            in 0..20 -> formalityBandDNA.find { it.tone == "Low" }
            in 21..40 -> formalityBandDNA.find { it.tone == "Low-Mid" }
            in 41..60 -> formalityBandDNA.find { it.tone == "Mid" }
            in 61..80 -> formalityBandDNA.find { it.tone == "Mid-High" }
            in 81..100 -> formalityBandDNA.find { it.tone == "High" }
            else -> null
        }
    }
    
    /**
     * Get all available tone names
     */
    fun getAvailableTones(): List<String> {
        return toneSpecificDNA.map { it.tone }
    }
}

/**
 * Data class for voice settings with formality and polish levels
 */
data class VoiceSettings(
    val formality: Int, // 0-100 scale
    val polish: Int     // 0-100 scale
)

/**
 * Voice Engine 3.0 settings with tone selection and polish level
 */
data class VoiceEngine3Settings(
    val selectedTone: String,
    val polishLevel: Int // 0-100 scale
)

/**
 * Hardcoded Voice DNA for Iain (Voice Engine 2.0 compatibility)
 * @deprecated Use VoiceDNARepository to load Voice Engine 3.0 patterns
 */
@Deprecated("Use VoiceDNARepository for Voice Engine 3.0")
object IainVoiceDNA {
    val voiceDNA = VoiceDNA(
        tone = "Legacy",
        confidence = 1.0f,
        userFormalityRange = listOf(20, 80),
        theoreticalRange = listOf(0, 100),
        formalityShifts = """
            • Casual: sometimes lowercase ('yes.'), 'prob', 'honey', often no "?", no greeting.  
            • Mid: "Hey Sam," with mixed fragments.  
            • Formal: "Hi Sam.", caps on, context before ask ("From our call last week…"), sign-off "Thanks,\nIain".  
            Greeting ladder: none → Hey → Hi (or "Team" for groups).
        """.trimIndent(),

        polishPatterns = """
            • Rushed: all-lower, sparse punctuation, fragments ("just tried calling… no answer").  
            • Polished: paragraphs / numbered or dashed lists, clear line breaks, decision first → reasoning ("All good.\nLet's do next week.").
        """.trimIndent(),

        constants = """
            Always: spaced em-dash ( – ), line break per thought, "We've got", short acks with period, action/decision first.
            Use "All good" only to reassure, confirm, or smooth over—not as a generic opener.
        """.trimIndent(),

        voiceMarkers = """
            Cues: "ish" times (4:30ish), sentence-end "sorry", questions sometimes drop "?", punchy sentences over commas.
        """.trimIndent(),
        
        antiPatterns = """
            Avoid: formal corporate language, excessive politeness, overly complex sentence structures.
        """.trimIndent(),
        
        sourceExampleIds = listOf()
    )
} 