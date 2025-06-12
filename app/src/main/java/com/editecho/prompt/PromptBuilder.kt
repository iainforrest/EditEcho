// src/main/java/com/editecho/prompt/PromptBuilder.kt
package com.editecho.prompt

/**
 * Builds the full system prompt that precedes the user's raw voice‑to‑text input.
 *
 * Order:
 *   1. BASE_SYSTEM   (context, role, task, constraints)
 *   2. Tone header + brief
 *   3. Authentic examples for that tone
 *   4. OUTPUT_GUIDELINES   – always last (stop‑token + fallback)
 */
@Deprecated("Replaced by VoicePromptBuilder")
object PromptBuilder {

    /** Assemble the system prompt for the chosen tone. */
    fun buildSystemPrompt(tone: ToneProfile): String {
        val sb = StringBuilder()

        // 1️⃣ invariant base rules
        sb.append(PromptAssets.BASE_SYSTEM).append("\n\n")

        // 2️⃣ tone header + brief
        sb.append("## TONE\n${tone.displayName}\n\n")
        sb.append(getToneBrief(tone)).append("\n\n")

        // 3️⃣ authentic examples
        sb.append("## EXAMPLES\n")
        val examples = getToneExamples(tone)
        examples.forEach { sb.append("* ").append(it).append('\n') }
        sb.append("\n")

        // 4️⃣ output guidelines (last for recency weight)
        sb.append(PromptAssets.OUTPUT_GUIDELINES)

        return sb.toString()
    }

    /** Wrap raw user text so the model clearly sees the payload that must be edited. */
    fun wrapUserInput(raw: String): String =
        "Here's the raw message to improve:\n$raw"

    private fun getToneBrief(tone: ToneProfile): String = when (tone) {
        ToneProfile.CASUAL -> PromptAssets.Briefs.FRIENDLY
        ToneProfile.NEUTRAL -> PromptAssets.Briefs.DIRECT  
        ToneProfile.INFORMATIVE -> PromptAssets.Briefs.ENGAGED
        ToneProfile.SUPPORTIVE -> PromptAssets.Briefs.FRIENDLY
        ToneProfile.THOUGHTFUL -> PromptAssets.Briefs.REFLECTIVE
    }

    private fun getToneExamples(tone: ToneProfile): List<String> = when (tone) {
        ToneProfile.CASUAL -> PromptAssets.Examples.FRIENDLY
        ToneProfile.NEUTRAL -> PromptAssets.Examples.DIRECT
        ToneProfile.INFORMATIVE -> PromptAssets.Examples.ENGAGED
        ToneProfile.SUPPORTIVE -> PromptAssets.Examples.FRIENDLY
        ToneProfile.THOUGHTFUL -> PromptAssets.Examples.REFLECTIVE
    }
}
