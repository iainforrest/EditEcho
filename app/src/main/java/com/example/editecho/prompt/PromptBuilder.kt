package com.example.editecho.prompt

object PromptBuilder {

    fun buildSystemPrompt(tone: String): String {
        val examples = when (tone) {
            "SMS" -> ToneProfile.smsExamples
            "Email" -> ToneProfile.emailExamples
            "Pro" -> ToneProfile.proExamples
            else -> emptyList()
        }

        return """
            ${PromptConfig.baseSystemPrompt}

            ${PromptConfig.editingInstructions}

            Additional Style Rules:
            ${ToneProfile.styleRules}

            Example Messages for ${tone.uppercase()} tone:
            ${examples.joinToString("\n- ", prefix = "- ")}
        """.trimIndent()
    }
}
