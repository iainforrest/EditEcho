package com.editecho.prompt

/**
 * Builder for creating voice-aware prompts using Voice DNA and voice settings
 * 
 * Example output for VoiceSettings(1,1) with "test text":
 * ```
 * You are editing a voice transcription. The speaker's patterns are:
 * 
 * FORMALITY PATTERNS (apply at level 1/5):
 * When casual, Iain drops capital letters entirely ("yes. talked to franki about this last week")...
 * 
 * POLISH PATTERNS (apply at level 1/5):
 * In rushed messages, Iain writes stream-of-consciousness style with minimal punctuation...
 * 
 * MUST PRESERVE:
 * Regardless of context, Iain always uses line breaks to separate distinct thoughts...
 * 
 * UNIQUE MARKERS TO KEEP:
 * Iain's Kiwi voice shines through in phrases like "you reckon"...
 * 
 * At formality 1/5 and polish 1/5, edit this text:
 * test text
 * ```
 * 
 * VERIFICATION: The buildPrompt function generates prompts that include:
 * ✓ All four Voice DNA components (formalityShifts, polishPatterns, constants, voiceMarkers)
 * ✓ Dynamic formality level (1-5) 
 * ✓ Dynamic polish level (1-5)
 * ✓ The complete raw text to be edited
 * ✓ Proper formatting with clear section headers
 * ✓ Full access to IainVoiceDNA.voiceDNA properties
 */
object VoicePromptBuilder {
    
    /**
     * Builds a prompt for text editing using voice settings and Voice DNA patterns
     * 
     * @param voiceSettings The formality and polish levels (1-5)
     * @param rawText The raw transcribed text to be edited
     * @return A formatted prompt string for the AI containing all Voice DNA patterns
     */
    fun buildPrompt(voiceSettings: VoiceSettings, rawText: String): String {
        return """You are editing a voice transcription. The speaker's patterns are:

FORMALITY PATTERNS (apply at level ${voiceSettings.formality}/5):
${IainVoiceDNA.voiceDNA.formalityShifts}

POLISH PATTERNS (apply at level ${voiceSettings.polish}/5):
${IainVoiceDNA.voiceDNA.polishPatterns}

MUST PRESERVE:
${IainVoiceDNA.voiceDNA.constants}

UNIQUE MARKERS TO KEEP:
${IainVoiceDNA.voiceDNA.voiceMarkers}

At formality ${voiceSettings.formality}/5 and polish ${voiceSettings.polish}/5, edit this text:
$rawText"""
    }
    
    /**
     * Test function to verify the prompt builder works correctly
     * 
     * This function can be called to test various combinations of voice settings
     * and verify that all Voice DNA components are properly included in the output.
     */
    fun testPromptBuilder() {
        val testSettings = VoiceSettings(formality = 1, polish = 1)
        val testText = "test text"
        val result = buildPrompt(testSettings, testText)
        
        println("=== VoicePromptBuilder Test ===")
        println("Settings: Formality ${testSettings.formality}/5, Polish ${testSettings.polish}/5")
        println("Input text: \"$testText\"")
        println("\n=== Generated Prompt ===")
        println(result)
        println("\n=== Test Verification ===")
        println("✓ Contains formality patterns: ${result.contains("FORMALITY PATTERNS")}")
        println("✓ Contains polish patterns: ${result.contains("POLISH PATTERNS")}")
        println("✓ Contains constants: ${result.contains("MUST PRESERVE")}")
        println("✓ Contains voice markers: ${result.contains("UNIQUE MARKERS TO KEEP")}")
        println("✓ Contains correct formality level: ${result.contains("formality 1/5")}")
        println("✓ Contains correct polish level: ${result.contains("polish 1/5")}")
        println("✓ Contains input text: ${result.contains(testText)}")
        
        // Test different settings
        println("\n=== Testing Different Settings ===")
        val highSettings = VoiceSettings(formality = 5, polish = 5)
        val highPrompt = buildPrompt(highSettings, "professional meeting notes")
        println("High formality/polish test:")
        println("✓ Contains 5/5 levels: ${highPrompt.contains("formality 5/5") && highPrompt.contains("polish 5/5")}")
        println("✓ Contains input text: ${highPrompt.contains("professional meeting notes")}")
    }
}

/**
 * Main function for testing the VoicePromptBuilder
 * Can be called directly to test the prompt generation
 */
fun main() {
    VoicePromptBuilder.testPromptBuilder()
} 