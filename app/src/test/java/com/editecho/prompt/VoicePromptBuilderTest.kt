package com.editecho.prompt

import com.editecho.data.VoiceDNARepository
import com.editecho.util.FormalityMapper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

/**
 * Comprehensive unit tests for Voice Engine 3.0 VoicePromptBuilder
 * Tests tone/polish combinations, confidence-based DNA selection, and prompt formatting
 */
class VoicePromptBuilderTest {

    // ─── Voice Engine 2.0 Legacy Tests ─────────────────────────────────────
    
    @Test
    fun testPromptBuilderWithMinimumValues() {
        val settings = VoiceSettings(formality = 0, polish = 0)
        val testText = "hey mate just wanted to check if you reckon we should prob go with option a"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertThat(prompt).contains("0/100")
        assertThat(prompt).contains(testText)
        assertThat(prompt).contains("FORMALITY PATTERNS")
    }

    @Test
    fun testPromptBuilderWithMiddleValues() {
        val settings = VoiceSettings(formality = 50, polish = 50)
        val testText = "testing middle range values"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertThat(prompt).contains("50/100")
        assertThat(prompt).contains(testText)
    }

    @Test
    fun testPromptBuilderWithMaximumValues() {
        val settings = VoiceSettings(formality = 100, polish = 100)
        val testText = "formal and highly polished message"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertThat(prompt).contains("100/100")
        assertThat(prompt).contains(testText)
    }

    @Test
    fun testPromptBuilderWithAsymmetricValues() {
        val settings = VoiceSettings(formality = 25, polish = 75)
        val testText = "asymmetric formality and polish levels"
        
        val prompt = VoicePromptBuilder.buildPrompt(settings, testText)
        
        assertThat(prompt).contains("25/100")
        assertThat(prompt).contains("75/100")
    }

    @Test
    fun testDefaultVoiceSettings() {
        val defaultSettings = VoiceSettings(formality = 50, polish = 50)
        
        assertThat(defaultSettings.formality).isEqualTo(50)
        assertThat(defaultSettings.polish).isEqualTo(50)
    }
    
    // ─── Voice Engine 3.0 Core Tests ──────────────────────────────────────
    
    @Test
    fun `buildPrompt should use high confidence tone DNA when available`() {
        // Mock repository and DNA
        val mockRepository = mockk<VoiceDNARepository>()
        val highConfidenceDNA = createTestToneDNA("Casual", 0.8f)
        val formalityBandDNA = createTestFormalityBandDNA("Low-Mid", 0.5f)
        
        every { mockRepository.getToneDNA("Casual") } returns highConfidenceDNA
        every { mockRepository.getFormalityBandDNA(25) } returns formalityBandDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.CASUAL,
            polishLevel = 50,
            rawText = "hey mate whats up",
            repository = mockRepository
        )
        
        // Should use high confidence tone DNA
        assertThat(prompt).contains("TONE DNA PATTERNS (Casual, confidence 0.8)")
        assertThat(prompt).contains("FORMALITY SHIFTS:")
        assertThat(prompt).contains("POLISH PATTERNS:")
        assertThat(prompt).contains("MUST PRESERVE:")
        assertThat(prompt).contains("UNIQUE MARKERS TO KEEP:")
        assertThat(prompt).contains("AVOID:")
        assertThat(prompt).contains("TARGET: Casual tone at 25% formality, 50% polish")
        assertThat(prompt).contains("hey mate whats up")
    }
    
    @Test
    fun `buildPrompt should use formality band DNA when tone confidence is low`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val lowConfidenceDNA = createTestToneDNA("Supportive", 0.3f)
        val formalityBandDNA = createTestFormalityBandDNA("Low-Mid", 0.5f)
        
        every { mockRepository.getToneDNA("Supportive") } returns lowConfidenceDNA
        every { mockRepository.getFormalityBandDNA(40) } returns formalityBandDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.SUPPORTIVE,
            polishLevel = 50,
            rawText = "i think we should help them out",
            repository = mockRepository
        )
        
        // Should use formality band DNA as primary
        assertThat(prompt).contains("FORMALITY BAND PATTERNS (Low-Mid - 40%, confidence 0.5)")
        assertThat(prompt).contains("TONE INTENT (Supportive, low confidence 0.3)")
        assertThat(prompt).contains("Apply Supportive communication intent")
        assertThat(prompt).contains("TARGET: Supportive intent at Low-Mid formality (40%), 50% polish")
        assertThat(prompt).contains("i think we should help them out")
    }
    
    @Test
    fun `buildPrompt should use fallback when no DNA patterns available`() {
        val mockRepository = mockk<VoiceDNARepository>()
        
        every { mockRepository.getToneDNA("Neutral") } returns null
        every { mockRepository.getFormalityBandDNA(45) } returns null
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.NEUTRAL,
            polishLevel = 50,
            rawText = "this is a test message",
            repository = mockRepository
        )
        
        // Should use fallback prompt
        assertThat(prompt).contains("TONE GUIDANCE: Neutral - Plain, factual updates without subjective stance")
        assertThat(prompt).contains("FORMALITY GUIDANCE: Relaxed professional communication")
        assertThat(prompt).contains("POLISH LEVEL: 50% (simple → structured)")
        assertThat(prompt).contains("TARGET: Transform text to match Neutral tone at 45% formality, 50% polish")
        assertThat(prompt).contains("this is a test message")
    }
    
    @Test
    fun `buildPrompt with tone name should work for valid tones`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Informative", 0.6f)
        val formalityBandDNA = createTestFormalityBandDNA("Mid-High", 0.4f)
        
        every { mockRepository.getToneDNA("Informative") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(56) } returns formalityBandDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(
            toneName = "Informative",
            polishLevel = 75,
            rawText = "let me explain how this works",
            repository = mockRepository
        )
        
        assertThat(prompt).isNotNull()
        assertThat(prompt).contains("FORMALITY BAND PATTERNS (Mid-High - 56%, confidence 0.4)")
        assertThat(prompt).contains("TONE INTENT (Informative, low confidence 0.6)")
    }
    
    @Test
    fun `buildPrompt with tone name should return null for invalid tones`() {
        val mockRepository = mockk<VoiceDNARepository>()
        
        val prompt = VoicePromptBuilder.buildPrompt(
            toneName = "InvalidTone",
            polishLevel = 50,
            rawText = "test text",
            repository = mockRepository
        )
        
        assertThat(prompt).isNull()
    }
    
    @Test
    fun `buildPrompt should use custom confidence threshold`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val mediumConfidenceDNA = createTestToneDNA("Thoughtful", 0.6f)
        val formalityBandDNA = createTestFormalityBandDNA("Mid-High", 0.4f)
        
        every { mockRepository.getToneDNA("Thoughtful") } returns mediumConfidenceDNA
        every { mockRepository.getFormalityBandDNA(58) } returns formalityBandDNA
        
        // With default threshold (0.7): should use formality band DNA
        val promptDefault = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.THOUGHTFUL,
            polishLevel = 70,
            rawText = "i've been thinking about this",
            repository = mockRepository
        )
        assertThat(promptDefault).contains("FORMALITY BAND PATTERNS")
        
        // With custom threshold (0.5): should use tone DNA
        val promptCustom = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.THOUGHTFUL,
            polishLevel = 70,
            rawText = "i've been thinking about this",
            repository = mockRepository,
            confidenceThreshold = 0.5f
        )
        assertThat(promptCustom).contains("TONE DNA PATTERNS (Thoughtful, confidence 0.6)")
    }
    
    // ─── Tone-Specific Formality Tests ────────────────────────────────────
    
    @Test
    fun `buildPrompt should calculate correct formality for each tone`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Test", 0.8f)
        
        every { mockRepository.getToneDNA(any()) } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        
        // Test Casual tone formality calculation
        val casualPrompt = VoicePromptBuilder.buildPrompt(ToneProfile.CASUAL, 50, "test", mockRepository)
        assertThat(casualPrompt).contains("TARGET: Casual tone at 25% formality, 50% polish")
        
        // Test Neutral tone formality calculation  
        val neutralPrompt = VoicePromptBuilder.buildPrompt(ToneProfile.NEUTRAL, 50, "test", mockRepository)
        assertThat(neutralPrompt).contains("TARGET: Neutral tone at 45% formality, 50% polish")
        
        // Test Informative tone formality calculation
        val informativePrompt = VoicePromptBuilder.buildPrompt(ToneProfile.INFORMATIVE, 50, "test", mockRepository)
        assertThat(informativePrompt).contains("TARGET: Informative tone at 50% formality, 50% polish")
    }
    
    @Test
    fun `buildPrompt should preserve CRITICAL RULES in all scenarios`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Test", 0.8f)
        
        every { mockRepository.getToneDNA(any()) } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        
        val prompt = VoicePromptBuilder.buildPrompt(ToneProfile.CASUAL, 50, "test", mockRepository)
        
        // Verify CRITICAL RULES are preserved
        assertThat(prompt).contains("CRITICAL RULES:")
        assertThat(prompt).contains("Keep speaker's meaning + style. No new content.")
        assertThat(prompt).contains("OK to reorder for clarity.")
        assertThat(prompt).contains("Kill exact dupes, ums/uhs")
        assertThat(prompt).contains("Len = input –15 % / +10 %")
        assertThat(prompt).contains("Output text only.")
    }
    
    @Test
    fun `buildPrompt should include formality band guidance when available`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Casual", 0.8f)
        val formalityBandDNA = createTestFormalityBandDNA("Low-Mid", 0.5f)
        
        every { mockRepository.getToneDNA("Casual") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(25) } returns formalityBandDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(ToneProfile.CASUAL, 50, "test", mockRepository)
        
        assertThat(prompt).contains("FORMALITY BAND GUIDANCE (Low-Mid - 25%)")
        assertThat(prompt).contains("Test formality band shifts")
    }
    
    // ─── Integration and Edge Case Tests ───────────────────────────────────
    
    @Test
    fun `getVoiceEngine3Settings should return correct settings`() {
        val settings = VoicePromptBuilder.getVoiceEngine3Settings(ToneProfile.THOUGHTFUL, 75)
        
        assertThat(settings.selectedTone).isEqualTo("Thoughtful")
        assertThat(settings.polishLevel).isEqualTo(75)
    }
    
    @Test
    fun `testVoiceEngine3PromptBuilder should work with repository`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Casual", 0.8f)
        
        every { mockRepository.getToneDNA("Casual") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        
        val testPrompt = VoicePromptBuilder.testVoiceEngine3PromptBuilder(mockRepository)
        
        assertThat(testPrompt).isNotEmpty()
        assertThat(testPrompt).contains("hey mate just wanted to check")
        assertThat(testPrompt).contains("TONE DNA PATTERNS")
    }
    
    @Test
    fun `buildPrompt should handle edge polish levels correctly`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Neutral", 0.8f)
        
        every { mockRepository.getToneDNA("Neutral") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        
        // Test minimum polish (0%)
        val minPrompt = VoicePromptBuilder.buildPrompt(ToneProfile.NEUTRAL, 0, "test", mockRepository)
        assertThat(minPrompt).contains("TARGET: Neutral tone at 20% formality, 0% polish")
        
        // Test maximum polish (100%)
        val maxPrompt = VoicePromptBuilder.buildPrompt(ToneProfile.NEUTRAL, 100, "test", mockRepository)
        assertThat(maxPrompt).contains("TARGET: Neutral tone at 70% formality, 100% polish")
    }
    
    @Test
    fun `buildPrompt should handle missing fallback DNA gracefully`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Supportive", 0.3f) // Low confidence
        
        every { mockRepository.getToneDNA("Supportive") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null // No formality band DNA
        
        val prompt = VoicePromptBuilder.buildPrompt(ToneProfile.SUPPORTIVE, 50, "test", mockRepository)
        
        // Should fall back to fallback prompt
        assertThat(prompt).contains("TONE GUIDANCE: Supportive - Empathetic and reassuring communication")
        assertThat(prompt).contains("FORMALITY GUIDANCE:")
        assertThat(prompt).contains("POLISH LEVEL: 50%")
    }
    
    // ─── Test Helper Methods ───────────────────────────────────────────────
    
    private fun createTestToneDNA(toneName: String, confidence: Float): VoiceDNA {
        return VoiceDNA(
            tone = toneName,
            confidence = confidence,
            userFormalityRange = listOf(20, 60),
            theoreticalRange = listOf(15, 75),
            formalityShifts = "Test tone formality shifts for $toneName",
            polishPatterns = "Test tone polish patterns for $toneName",
            constants = "Test tone constants for $toneName",
            voiceMarkers = "Test tone voice markers for $toneName",
            antiPatterns = "Test tone anti-patterns for $toneName",
            sourceExampleIds = listOf(1, 2, 3)
        )
    }
    
    private fun createTestFormalityBandDNA(bandName: String, confidence: Float): VoiceDNA {
        return VoiceDNA(
            tone = bandName,
            confidence = confidence,
            userFormalityRange = listOf(25, 45),
            theoreticalRange = listOf(21, 50),
            formalityShifts = "Test formality band shifts for $bandName",
            polishPatterns = "Test formality band polish patterns for $bandName",
            constants = "Test formality band constants for $bandName",
            voiceMarkers = "Test formality band voice markers for $bandName",
            antiPatterns = "Test formality band anti-patterns for $bandName",
            sourceExampleIds = listOf(4, 5, 6)
        )
    }
    
    private fun createTestUniversalDNA(): UniversalDNA {
        return UniversalDNA(
            confidence = 0.75f,
            description = "Core patterns that persist across all communication",
            patterns = listOf(
                "Strong preference for digits over spelled numbers",
                "Heavy contraction use regardless of formality (I'll, I'm, won't, can't)",
                "Line breaks separate distinct thoughts more than traditional paragraphs",
                "Time format always digital with colon (6:15, 4:30)"
            ),
            warnings = listOf(
                "Based on 20 examples spanning casual to mid-high formality",
                "Question mark omission should be applied sparingly"
            )
        )
    }
    
    // ─── Universal DNA Integration Tests ───────────────────────────────────
    
    @Test
    fun `buildPrompt should include universal DNA in high confidence tone prompts`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Casual", 0.8f)
        val universalDNA = createTestUniversalDNA()
        
        every { mockRepository.getToneDNA("Casual") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        every { mockRepository.getUniversalDNA() } returns universalDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.CASUAL,
            polishLevel = 50,
            rawText = "hey mate whats up",
            repository = mockRepository
        )
        
        // Should include universal DNA section
        assertThat(prompt).contains("UNIVERSAL VOICE RULES (Apply to all edits, but specific tone rules take priority):")
        assertThat(prompt).contains("- Strong preference for digits over spelled numbers")
        assertThat(prompt).contains("- Heavy contraction use regardless of formality (I'll, I'm, won't, can't)")
        assertThat(prompt).contains("- Line breaks separate distinct thoughts more than traditional paragraphs")
        assertThat(prompt).contains("- Time format always digital with colon (6:15, 4:30)")
        
        // Should also include tone-specific patterns
        assertThat(prompt).contains("TONE DNA PATTERNS (Casual, confidence 0.8)")
    }
    
    @Test
    fun `buildPrompt should include universal DNA in low confidence formality band prompts`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Supportive", 0.3f)
        val formalityBandDNA = createTestFormalityBandDNA("Low-Mid", 0.5f)
        val universalDNA = createTestUniversalDNA()
        
        every { mockRepository.getToneDNA("Supportive") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(40) } returns formalityBandDNA
        every { mockRepository.getUniversalDNA() } returns universalDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.SUPPORTIVE,
            polishLevel = 50,
            rawText = "i think we should help them out",
            repository = mockRepository
        )
        
        // Should include universal DNA section
        assertThat(prompt).contains("UNIVERSAL VOICE RULES (Apply to all edits, but specific tone rules take priority):")
        assertThat(prompt).contains("- Strong preference for digits over spelled numbers")
        assertThat(prompt).contains("- Heavy contraction use regardless of formality (I'll, I'm, won't, can't)")
        
        // Should also include formality band patterns
        assertThat(prompt).contains("FORMALITY BAND PATTERNS (Low-Mid - 40%, confidence 0.5)")
    }
    
    @Test
    fun `buildPrompt should include universal DNA in fallback prompts`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val universalDNA = createTestUniversalDNA()
        
        every { mockRepository.getToneDNA("Neutral") } returns null
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        every { mockRepository.getUniversalDNA() } returns universalDNA
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.NEUTRAL,
            polishLevel = 50,
            rawText = "this is a test message",
            repository = mockRepository
        )
        
        // Should include universal DNA section
        assertThat(prompt).contains("UNIVERSAL VOICE RULES (Apply to all edits, but specific tone rules take priority):")
        assertThat(prompt).contains("- Strong preference for digits over spelled numbers")
        assertThat(prompt).contains("- Time format always digital with colon (6:15, 4:30)")
        
        // Should also include fallback guidance
        assertThat(prompt).contains("TONE GUIDANCE: Neutral - Plain, factual updates without subjective stance")
    }
    
    @Test
    fun `buildPrompt should handle missing universal DNA gracefully`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = createTestToneDNA("Casual", 0.8f)
        
        every { mockRepository.getToneDNA("Casual") } returns toneDNA
        every { mockRepository.getFormalityBandDNA(any()) } returns null
        every { mockRepository.getUniversalDNA() } returns null
        
        val prompt = VoicePromptBuilder.buildPrompt(
            tone = ToneProfile.CASUAL,
            polishLevel = 50,
            rawText = "hey mate whats up",
            repository = mockRepository
        )
        
        // Should not include universal DNA section when not available
        assertThat(prompt).doesNotContain("UNIVERSAL VOICE RULES")
        
        // Should still include tone-specific patterns
        assertThat(prompt).contains("TONE DNA PATTERNS (Casual, confidence 0.8)")
    }
} 