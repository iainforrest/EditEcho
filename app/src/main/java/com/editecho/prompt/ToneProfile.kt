// src/main/java/com/editecho/prompt/ToneProfile.kt
package com.editecho.prompt

/**
 * Intent–based tone categories.
 */
@Deprecated("Replaced by VoiceSettings")
enum class ToneProfile(
    val displayName: String,
    val category: String,
    val description: String
) {
    FRIENDLY("Friendly", "Casual/Personal", "A warm, upbeat tone used when speaking to close friends or peers."),
    ENGAGED("Engaged", "Balanced/Informative", "A balanced and informative tone conveying organized thought with a friendly twist."),
    DIRECT("Direct", "Professional/Concise", "A straightforward and professional tone for clear and concise communication."),
    REFLECTIVE("Reflective", "Thoughtful/Introspective", "A thoughtful and introspective tone with a touch of warmth and sincerity."),
    TRANSCRIBE_ONLY("Transcribe Only", "Raw/Unedited", "Get the raw transcription without any AI editing.");

    companion object {
        fun fromName(name: String): ToneProfile =
            values().firstOrNull { it.displayName == name } ?: FRIENDLY
    }
}
