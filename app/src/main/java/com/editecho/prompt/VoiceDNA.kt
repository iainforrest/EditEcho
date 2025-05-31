package com.editecho.prompt

/**
 * Data class representing the core components of a person's voice DNA
 */
data class VoiceDNA(
    val formalityShifts: String,
    val polishPatterns: String,
    val constants: String,
    val voiceMarkers: String
)

/**
 * Data class for voice settings with formality and polish levels
 */
data class VoiceSettings(
    val formality: Int, // 0-100 scale
    val polish: Int     // 0-100 scale
)

/**
 * Hardcoded Voice DNA for Iain
 */
object IainVoiceDNA {
    val voiceDNA = VoiceDNA(
    formalityShifts = """
        • Casual: sometimes lowercase (‘yes.’), ‘prob’, ‘honey’, often no “?”, no greeting.  
        • Mid: “Hey Sam,” with mixed fragments.  
        • Formal: “Hi Sam.”, caps on, context before ask (“From our call last week…”), sign-off “Thanks,\nIain”.  
        Greeting ladder: none → Hey → Hi (or “Team” for groups).
    """.trimIndent(),

    polishPatterns = """
        • Rushed: all-lower, sparse punctuation, fragments (“just tried calling… no answer”).  
        • Polished: paragraphs / numbered or dashed lists, clear line breaks, decision first → reasoning (“All good.\nLet’s do next week.”).
    """.trimIndent(),

    constants = """
        Always: spaced em-dash ( – ), line break per thought, “We’ve got”, short acks with period, action/decision first.
        Use “All good” only to reassure, confirm, or smooth over—not as a generic opener.
    """.trimIndent(),

    voiceMarkers = """
        Cues: “ish” times (4:30ish), sentence-end “sorry”, questions sometimes drop “?”, punchy sentences over commas.
    """.trimIndent()
)
} 