// src/main/java/com/example/editecho/prompt/PromptBuilder.kt
package com.example.editecho.prompt

/**
 * Builds the system + tone mini-brief + examples block sent ahead of user content.
 */
object PromptBuilder {

    fun buildSystemPrompt(tone: ToneProfile): String = buildString {
        appendLine(PromptAssets.BASE_SYSTEM)
        appendLine()
        appendLine(PromptAssets.EDITING_GUIDELINES)
        appendLine()
        appendLine(PromptAssets.STYLE_RULES)
        appendLine()

        appendLine("Tone: ${tone.systemLabel}")
        val brief = when (tone) {
            ToneProfile.QUICK    -> PromptAssets.Briefs.QUICK
            ToneProfile.FRIENDLY -> PromptAssets.Briefs.FRIENDLY
            ToneProfile.POLISHED -> PromptAssets.Briefs.POLISHED
        }
        appendLine("Guidelines for this tone → $brief")
        appendLine()

        appendLine("Authentic examples:")
        val examples = when (tone) {
            ToneProfile.QUICK    -> PromptAssets.Examples.QUICK
            ToneProfile.FRIENDLY -> PromptAssets.Examples.FRIENDLY
            ToneProfile.POLISHED -> PromptAssets.Examples.POLISHED
        }
        examples.take(4).forEach { appendLine("• $it") }
    }
}
