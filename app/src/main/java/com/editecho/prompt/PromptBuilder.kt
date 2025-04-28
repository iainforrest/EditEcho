// src/main/java/com/editecho/prompt/PromptBuilder.kt
package com.editecho.prompt

/**
 * Builds the system + tone mini-brief + examples block sent ahead of user content.
 */
object PromptBuilder {

    /**
     * Builds a complete system prompt for the ChatGPT API based on the selected tone.
     * 
     * @param tone The selected tone profile (FAMILIAR, DIRECT, COLLABORATIVE, or PROFESSIONAL)
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
            ToneProfile.FAMILIAR -> PromptAssets.Briefs.FAMILIAR
            ToneProfile.DIRECT -> PromptAssets.Briefs.DIRECT
            ToneProfile.COLLABORATIVE -> PromptAssets.Briefs.COLLABORATIVE
            ToneProfile.PROFESSIONAL -> PromptAssets.Briefs.PROFESSIONAL
        }
        val briefSection = "Guidelines for this tone → $brief"
        
        // Get the appropriate examples based on tone
        val examples = when (tone) {
            ToneProfile.FAMILIAR -> PromptAssets.Examples.FAMILIAR
            ToneProfile.DIRECT -> PromptAssets.Examples.DIRECT
            ToneProfile.COLLABORATIVE -> PromptAssets.Examples.COLLABORATIVE
            ToneProfile.PROFESSIONAL -> PromptAssets.Examples.PROFESSIONAL
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
