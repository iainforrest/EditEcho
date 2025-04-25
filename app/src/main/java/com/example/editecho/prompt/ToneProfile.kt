package com.example.editecho.prompt

/**
 * Enum representing different tone profiles for text refinement.
 * Each profile has a short display name for UI and a full label for API prompts.
 */
enum class ToneProfile(val displayName: String, val fullLabel: String) {
    QUICK("Quick", "Quick Message"),
    FRIENDLY("Friendly", "Friendly Reply"),
    POLISHED("Polished", "Clear and Polished");

    companion object {
        fun fromDisplayName(name: String): ToneProfile {
            return values().find { it.displayName == name } ?: FRIENDLY
        }
    }
}

object ToneProfileExamples {
    val smsExamples = listOf(
        "Sweet as, see ya soon.",
        "Nah all good mate.",
        "Shot for that, legend.",
        "Yo, running 10 late sorry."
    )

    val emailExamples = listOf(
        "Hey team, just a quick heads up...",
        "Let me know if that works for you.",
        "Cheers for sorting that out."
    )

    val proExamples = listOf(
        "Kia ora, appreciate the update.",
        "Happy to chat further if needed.",
        "Let me know if you'd like me to draft something up."
    )

    val styleRules = """
        - Keep it concise.
        - Use Kiwi slang when appropriate.
        - Always friendly and casual unless told otherwise.
        - Avoid corporate jargon.
        - Use contractions often (I'm, it's, can't).
    """.trimIndent()
}
