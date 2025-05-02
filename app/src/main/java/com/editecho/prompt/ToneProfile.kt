// src/main/java/com/editecho/prompt/ToneProfile.kt
package com.editecho.prompt

/**
 * Intent–based tone categories.
 */
enum class ToneProfile(
    val displayName: String,
    val systemLabel: String,
    val description: String
) {
    FAMILIAR("Familiar", "Casual/Personal", "Warm, relaxed tone for messages to family or close friends."),
    DIRECT("Direct", "Direct/Operational", "Short, clear updates for fast decisions or logistics."),
    COLLABORATIVE("Collaborative", "Collaborative/Planning", "Helpful and contextual tone for coordinating plans."),
    PROFESSIONAL("Professional", "Professional/Structured", "Respectful and clear communication for external or formal contacts."),
    TRANSCRIBE_ONLY("Transcribe Only", "Raw/Unedited", "Get the raw transcription without any AI editing.");

    companion object {
        fun fromDisplayName(name: String) =
            values().firstOrNull { it.displayName == name } ?: FAMILIAR
    }
}
