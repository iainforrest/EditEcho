// src/main/java/com/example/editecho/prompt/PromptBuilder.kt
package com.example.editecho.prompt

/**
 * Builds the system + tone mini-brief + examples block sent ahead of user content.
 */
object PromptBuilder {

    /**
     * Builds a complete system prompt for the ChatGPT API based on the selected tone.
     * 
     * @param tone The selected tone profile (QUICK, FRIENDLY, or POLISHED)
     * @return A formatted system prompt string
     */
    fun buildSystemPrompt(tone: ToneProfile): String {
        // Always include these base components
        val baseComponents = listOf(
            PromptAssets.BASE_SYSTEM,
            PromptAssets.EDITING_GUIDELINES,
            PromptAssets.STYLE_RULES
        )
        
        // Get tone-specific components
        val toneLabel = "Tone: ${tone.systemLabel}"
        
        // Get the appropriate brief based on tone
        val brief = when (tone) {
            ToneProfile.QUICK -> PromptAssets.Briefs.QUICK
            ToneProfile.FRIENDLY -> PromptAssets.Briefs.FRIENDLY
            ToneProfile.POLISHED -> PromptAssets.Briefs.POLISHED
        }
        val briefSection = "Guidelines for this tone → $brief"
        
        // Get the appropriate examples based on tone
        val examples = when (tone) {
            ToneProfile.QUICK -> PromptAssets.Examples.QUICK
            ToneProfile.FRIENDLY -> PromptAssets.Examples.FRIENDLY
            ToneProfile.POLISHED -> PromptAssets.Examples.POLISHED
        }
        
        // Format examples as bullet points
        val examplesSection = buildString {
            appendLine("Authentic examples:")
            examples.forEach { appendLine("• $it") }
        }
        
        // Combine all sections with double newlines
        return buildString {
            // Add base components
            baseComponents.forEach { 
                append(it)
                append("\n\n")
            }
            
            // Add tone label
            append(toneLabel)
            append("\n\n")
            
            // Add brief
            append(briefSection)
            append("\n\n")
            
            // Add examples
            append(examplesSection)
        }
    }
}
