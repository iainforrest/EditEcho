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
object PromptBuilder {

    /** Assemble the system prompt for the chosen tone. */
    fun buildSystemPrompt(tone: ToneProfile): String {
        val sb = StringBuilder()

        // 1️⃣ invariant base rules
        sb.append(PromptAssets.BASE_SYSTEM).append("\n\n")

        // 2️⃣ tone header + brief
        sb.append("## TONE\n${tone.systemLabel}\n\n")
        sb.append(
            when (tone) {
                ToneProfile.FAMILIAR      -> PromptAssets.Briefs.FAMILIAR
                ToneProfile.DIRECT        -> PromptAssets.Briefs.DIRECT
                ToneProfile.COLLABORATIVE -> PromptAssets.Briefs.COLLABORATIVE
                ToneProfile.PROFESSIONAL  -> PromptAssets.Briefs.PROFESSIONAL
                ToneProfile.TRANSCRIBE_ONLY -> throw IllegalStateException("buildSystemPrompt should not be called for TRANSCRIBE_ONLY tone")
            }
        ).append("\n\n")

        // 3️⃣ authentic examples
        sb.append("## EXAMPLES\n")
        val examples = when (tone) {
            ToneProfile.FAMILIAR      -> PromptAssets.Examples.FAMILIAR
            ToneProfile.DIRECT        -> PromptAssets.Examples.DIRECT
            ToneProfile.COLLABORATIVE -> PromptAssets.Examples.COLLABORATIVE
            ToneProfile.PROFESSIONAL  -> PromptAssets.Examples.PROFESSIONAL
            ToneProfile.TRANSCRIBE_ONLY -> throw IllegalStateException("buildSystemPrompt should not be called for TRANSCRIBE_ONLY tone")
        }
        examples.forEach { sb.append("* ").append(it).append('\n') }
        sb.append("\n")

        // 4️⃣ output guidelines (last for recency weight)
        sb.append(PromptAssets.OUTPUT_GUIDELINES)

        return sb.toString()
    }

    /** Wrap raw user text so the model clearly sees the payload that must be edited. */
    fun wrapUserInput(raw: String): String =
        "Here's the raw message to improve:\n$raw"
}
