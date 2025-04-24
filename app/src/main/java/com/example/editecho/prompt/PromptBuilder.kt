package com.example.editecho.prompt

object PromptBuilder {

    fun buildSystemPrompt(tone: ToneProfile): String {
        val examples = when (tone) {
            ToneProfile.SMS -> ToneProfileExamples.smsExamples
            ToneProfile.Email -> ToneProfileExamples.emailExamples
            ToneProfile.Professional -> ToneProfileExamples.proExamples
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
