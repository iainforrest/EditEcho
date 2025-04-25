package com.example.editecho.prompt

object PromptBuilder {

    fun getTonePrompt(tone: ToneProfile): String {
        return when (tone) {
            ToneProfile.QUICK -> "Write a quick, concise message"
            ToneProfile.FRIENDLY -> "Write a friendly, casual reply"
            ToneProfile.POLISHED -> "Write a clear and polished message"
        }
    }

    fun buildSystemPrompt(tone: ToneProfile): String {
        val examples = when (tone) {
            ToneProfile.QUICK -> listOf(
                "Running 5 mins late, be there soon!",
                "Got it, thanks for the heads up.",
                "All good, catch you later!"
            )
            ToneProfile.FRIENDLY -> listOf(
                "Hey! Just wanted to check in and see how you're doing.",
                "That sounds great! Let me know if you need anything else.",
                "Thanks for letting me know - really appreciate it!"
            )
            ToneProfile.POLISHED -> listOf(
                "I appreciate your prompt response regarding this matter.",
                "Thank you for bringing this to my attention. I'll look into it right away.",
                "I understand your concern and will address it accordingly."
            )
        }

        return """
            ${PromptConfig.baseSystemPrompt}

            ${PromptConfig.editingInstructions}

            Additional Style Rules:
            ${ToneProfileExamples.styleRules}

            Example Messages for ${tone.displayName} tone:
            ${examples.joinToString("\n- ", prefix = "- ")}
        """.trimIndent()
    }
}
