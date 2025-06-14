package com.editecho.data

import com.editecho.prompt.VoiceDNA
import com.editecho.prompt.VoiceDNACollection
import com.editecho.prompt.UniversalDNA
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for VoiceDNARepository core logic
 * Tests confidence-based selection and pattern validation
 */
class VoiceDNARepositoryTest {
    
    // ─── Test Data ─────────────────────────────────────────────────────────
    
    private fun createTestVoiceDNACollection(): VoiceDNACollection {
        val toneSpecificDNA = listOf(
            VoiceDNA(
                tone = "Casual",
                confidence = 0.8f,
                userFormalityRange = listOf(15, 25),
                theoreticalRange = listOf(15, 35),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers with ish",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(1, 2, 3)
            ),
            VoiceDNA(
                tone = "Neutral",
                confidence = 0.3f,
                userFormalityRange = listOf(30, 50),
                theoreticalRange = listOf(20, 80),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(4, 5, 6)
            ),
            VoiceDNA(
                tone = "Informative",
                confidence = 0.5f,
                userFormalityRange = listOf(40, 60),
                theoreticalRange = listOf(25, 75),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Explains thought process",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(7, 8, 9)
            ),
            VoiceDNA(
                tone = "Supportive",
                confidence = 0.2f,
                userFormalityRange = listOf(20, 20),
                theoreticalRange = listOf(20, 60),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Collaborative language",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(10, 11)
            ),
            VoiceDNA(
                tone = "Thoughtful",
                confidence = 0.2f,
                userFormalityRange = listOf(45, 65),
                theoreticalRange = listOf(30, 80),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(12, 13)
            )
        )
        
        val formalityBandDNA = listOf(
            VoiceDNA(
                tone = "Low",
                confidence = 0.3f,
                userFormalityRange = listOf(15, 15),
                theoreticalRange = listOf(0, 20),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(14, 15)
            ),
            VoiceDNA(
                tone = "Low-Mid",
                confidence = 0.5f,
                userFormalityRange = listOf(25, 35),
                theoreticalRange = listOf(21, 40),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(16, 17)
            ),
            VoiceDNA(
                tone = "Mid",
                confidence = 0.4f,
                userFormalityRange = listOf(45, 55),
                theoreticalRange = listOf(41, 60),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(18, 19)
            ),
            VoiceDNA(
                tone = "Mid-High",
                confidence = 0.2f,
                userFormalityRange = listOf(60, 65),
                theoreticalRange = listOf(61, 80),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(20, 21)
            ),
            VoiceDNA(
                tone = "High",
                confidence = 0.0f,
                userFormalityRange = listOf(),
                theoreticalRange = listOf(81, 100),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf()
            )
        )
        
        return VoiceDNACollection(toneSpecificDNA, formalityBandDNA, null)
    }
    
    private fun createTestVoiceDNACollectionWithUniversal(): VoiceDNACollection {
        val toneSpecificDNA = listOf(
            VoiceDNA(
                tone = "Casual",
                confidence = 0.8f,
                userFormalityRange = listOf(15, 25),
                theoreticalRange = listOf(15, 35),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers with ish",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(1, 2, 3)
            )
        )
        
        val formalityBandDNA = listOf(
            VoiceDNA(
                tone = "Low",
                confidence = 0.3f,
                userFormalityRange = listOf(15, 15),
                theoreticalRange = listOf(0, 20),
                formalityShifts = "Test formality shifts",
                polishPatterns = "Test polish patterns",
                constants = "Test constants",
                voiceMarkers = "Test voice markers",
                antiPatterns = "Test anti patterns",
                sourceExampleIds = listOf(14, 15)
            )
        )
        
        val universalDNA = UniversalDNA(
            confidence = 0.75f,
            description = "Core patterns that persist across all communication regardless of tone or formality",
            patterns = listOf(
                "Strong preference for digits over spelled numbers",
                "Heavy contraction use regardless of formality (I'll, I'm, won't, can't)",
                "Line breaks separate distinct thoughts more than traditional paragraphs",
                "Time format always digital with colon (6:15, 4:30)"
            ),
            warnings = listOf(
                "Based on 20 examples spanning casual to mid-high formality",
                "Question mark omission should be applied sparingly and only in casual contexts"
            )
        )
        
        return VoiceDNACollection(toneSpecificDNA, formalityBandDNA, universalDNA)
    }
    
    // ─── VoiceDNACollection Tests ──────────────────────────────────────────
    
    @Test
    fun `VoiceDNACollection should have exactly 5 tone-specific DNA patterns`() {
        val collection = createTestVoiceDNACollection()
        
        assertThat(collection.toneSpecificDNA).hasSize(5)
        
        val expectedTones = listOf("Casual", "Neutral", "Informative", "Supportive", "Thoughtful")
        val actualTones = collection.toneSpecificDNA.map { it.tone }
        
        assertThat(actualTones).containsExactlyElementsIn(expectedTones)
    }
    
    @Test
    fun `VoiceDNACollection should have exactly 5 formality band DNA patterns`() {
        val collection = createTestVoiceDNACollection()
        
        assertThat(collection.formalityBandDNA).hasSize(5)
        
        val expectedBands = listOf("Low", "Low-Mid", "Mid", "Mid-High", "High")
        val actualBands = collection.formalityBandDNA.map { it.tone }
        
        assertThat(actualBands).containsExactlyElementsIn(expectedBands)
    }
    
    @Test
    fun `getToneDNA should return correct DNA for valid tone names`() {
        val collection = createTestVoiceDNACollection()
        
        val casualDNA = collection.getToneDNA("Casual")
        assertThat(casualDNA).isNotNull()
        assertThat(casualDNA?.tone).isEqualTo("Casual")
        assertThat(casualDNA?.confidence).isEqualTo(0.8f)
        
        val supportiveDNA = collection.getToneDNA("Supportive")
        assertThat(supportiveDNA).isNotNull()
        assertThat(supportiveDNA?.tone).isEqualTo("Supportive")
        assertThat(supportiveDNA?.confidence).isEqualTo(0.2f)
    }
    
    @Test
    fun `getToneDNA should be case insensitive`() {
        val collection = createTestVoiceDNACollection()
        
        val casualDNA1 = collection.getToneDNA("casual")
        val casualDNA2 = collection.getToneDNA("CASUAL")
        val casualDNA3 = collection.getToneDNA("Casual")
        
        assertThat(casualDNA1).isNotNull()
        assertThat(casualDNA2).isNotNull()
        assertThat(casualDNA3).isNotNull()
        
        assertThat(casualDNA1?.tone).isEqualTo(casualDNA2?.tone)
        assertThat(casualDNA2?.tone).isEqualTo(casualDNA3?.tone)
    }
    
    @Test
    fun `getToneDNA should return null for invalid tone names`() {
        val collection = createTestVoiceDNACollection()
        
        val invalidDNA = collection.getToneDNA("InvalidTone")
        assertThat(invalidDNA).isNull()
    }
    
    @Test
    fun `getFormalityBandDNA should return correct DNA for formality levels`() {
        val collection = createTestVoiceDNACollection()
        
        // Low band (0-20%)
        val lowDNA = collection.getFormalityBandDNA(10)
        assertThat(lowDNA?.tone).isEqualTo("Low")
        
        // Low-Mid band (21-40%)
        val lowMidDNA = collection.getFormalityBandDNA(30)
        assertThat(lowMidDNA?.tone).isEqualTo("Low-Mid")
        
        // Mid band (41-60%)
        val midDNA = collection.getFormalityBandDNA(50)
        assertThat(midDNA?.tone).isEqualTo("Mid")
        
        // Mid-High band (61-80%)
        val midHighDNA = collection.getFormalityBandDNA(70)
        assertThat(midHighDNA?.tone).isEqualTo("Mid-High")
        
        // High band (81-100%)
        val highDNA = collection.getFormalityBandDNA(90)
        assertThat(highDNA?.tone).isEqualTo("High")
    }
    
    @Test
    fun `getFormalityBandDNA should handle boundary values correctly`() {
        val collection = createTestVoiceDNACollection()
        
        // Test exact boundaries
        assertThat(collection.getFormalityBandDNA(0)?.tone).isEqualTo("Low")
        assertThat(collection.getFormalityBandDNA(20)?.tone).isEqualTo("Low")
        assertThat(collection.getFormalityBandDNA(21)?.tone).isEqualTo("Low-Mid")
        assertThat(collection.getFormalityBandDNA(40)?.tone).isEqualTo("Low-Mid")
        assertThat(collection.getFormalityBandDNA(41)?.tone).isEqualTo("Mid")
        assertThat(collection.getFormalityBandDNA(60)?.tone).isEqualTo("Mid")
        assertThat(collection.getFormalityBandDNA(61)?.tone).isEqualTo("Mid-High")
        assertThat(collection.getFormalityBandDNA(80)?.tone).isEqualTo("Mid-High")
        assertThat(collection.getFormalityBandDNA(81)?.tone).isEqualTo("High")
        assertThat(collection.getFormalityBandDNA(100)?.tone).isEqualTo("High")
    }
    
    @Test
    fun `getFormalityBandDNA should return null for invalid formality levels`() {
        val collection = createTestVoiceDNACollection()
        
        assertThat(collection.getFormalityBandDNA(-1)).isNull()
        assertThat(collection.getFormalityBandDNA(101)).isNull()
    }
    
    @Test
    fun `getAvailableTones should return all 5 tone names`() {
        val collection = createTestVoiceDNACollection()
        
        val tones = collection.getAvailableTones()
        
        assertThat(tones).hasSize(5)
        assertThat(tones).containsExactly("Casual", "Neutral", "Informative", "Supportive", "Thoughtful")
    }
    
    // ─── VoiceDNA Structure Tests ──────────────────────────────────────────
    
    @Test
    fun `all DNA patterns should have required fields populated`() {
        val collection = createTestVoiceDNACollection()
        
        // Check tone-specific DNA patterns
        collection.toneSpecificDNA.forEach { dna ->
            assertThat(dna.tone).isNotEmpty()
            assertThat(dna.confidence).isAtLeast(0.0f)
            assertThat(dna.confidence).isAtMost(1.0f)
            assertThat(dna.formalityShifts).isNotEmpty()
            assertThat(dna.polishPatterns).isNotEmpty()
            assertThat(dna.constants).isNotEmpty()
            assertThat(dna.voiceMarkers).isNotEmpty()
            assertThat(dna.antiPatterns).isNotEmpty()
            assertThat(dna.userFormalityRange).hasSize(2)
            assertThat(dna.theoreticalRange).hasSize(2)
        }
        
        // Check formality band DNA patterns  
        collection.formalityBandDNA.forEach { dna ->
            assertThat(dna.tone).isNotEmpty()
            assertThat(dna.confidence).isAtLeast(0.0f)
            assertThat(dna.confidence).isAtMost(1.0f)
            assertThat(dna.formalityShifts).isNotEmpty()
            assertThat(dna.polishPatterns).isNotEmpty()
            assertThat(dna.constants).isNotEmpty()
            assertThat(dna.voiceMarkers).isNotEmpty()
            assertThat(dna.antiPatterns).isNotEmpty()
        }
    }
    
    // ─── Confidence Threshold Tests ────────────────────────────────────────
    
    @Test
    fun `confidence threshold should be 0_7 as specified in PRD`() {
        assertThat(VoiceDNARepository.CONFIDENCE_THRESHOLD).isEqualTo(0.7f)
    }
    
    @Test
    fun `Casual tone should have high confidence above threshold`() {
        val collection = createTestVoiceDNACollection()
        val casualDNA = collection.getToneDNA("Casual")
        
        assertThat(casualDNA?.confidence).isEqualTo(0.8f)
        assertThat(casualDNA?.confidence).isAtLeast(VoiceDNARepository.CONFIDENCE_THRESHOLD)
    }
    
    @Test
    fun `Supportive and Thoughtful tones should have low confidence below threshold`() {
        val collection = createTestVoiceDNACollection()
        
        val supportiveDNA = collection.getToneDNA("Supportive")
        assertThat(supportiveDNA?.confidence).isEqualTo(0.2f)
        assertThat(supportiveDNA?.confidence).isLessThan(VoiceDNARepository.CONFIDENCE_THRESHOLD)
        
        val thoughtfulDNA = collection.getToneDNA("Thoughtful")
        assertThat(thoughtfulDNA?.confidence).isEqualTo(0.2f)
        assertThat(thoughtfulDNA?.confidence).isLessThan(VoiceDNARepository.CONFIDENCE_THRESHOLD)
    }
    
    @Test
    fun `High formality band should have zero confidence due to no examples`() {
        val collection = createTestVoiceDNACollection()
        val highDNA = collection.getFormalityBandDNA(90)
        
        assertThat(highDNA).isNotNull()
        assertThat(highDNA!!.tone).isEqualTo("High")
        assertThat(highDNA.confidence).isEqualTo(0.0f)
        assertThat(highDNA.sourceExampleIds).isEmpty()
    }
    
    // ─── Content Validation Tests ──────────────────────────────────────────
    
    @Test
    fun `patterns should contain expected voice characteristics`() {
        val collection = createTestVoiceDNACollection()
        
        // Validate that patterns contain expected voice characteristics
        val casualDNA = collection.toneSpecificDNA.find { it.tone == "Casual" }
        assertThat(casualDNA?.voiceMarkers).contains("ish")
        
        val informativeDNA = collection.toneSpecificDNA.find { it.tone == "Informative" }
        assertThat(informativeDNA?.voiceMarkers).contains("Explains thought process")
        
        val supportiveDNA = collection.toneSpecificDNA.find { it.tone == "Supportive" }
        assertThat(supportiveDNA?.voiceMarkers).contains("Collaborative language")
    }
    
    // ─── Universal DNA Tests ───────────────────────────────────────────────
    
    @Test
    fun `getUniversalDNA should return null when no universal DNA is present`() {
        val collection = createTestVoiceDNACollection()
        
        assertThat(collection.universalDNA).isNull()
    }
    
    @Test
    fun `getUniversalDNA should return expected data when universal DNA is present`() {
        val collection = createTestVoiceDNACollectionWithUniversal()
        
        val universalDNA = collection.universalDNA
        assertThat(universalDNA).isNotNull()
        assertThat(universalDNA!!.confidence).isEqualTo(0.75f)
        assertThat(universalDNA.description).contains("Core patterns that persist across all communication")
        assertThat(universalDNA.patterns).hasSize(4)
        assertThat(universalDNA.patterns).contains("Strong preference for digits over spelled numbers")
        assertThat(universalDNA.patterns).contains("Heavy contraction use regardless of formality (I'll, I'm, won't, can't)")
        assertThat(universalDNA.warnings).hasSize(2)
        assertThat(universalDNA.warnings).contains("Based on 20 examples spanning casual to mid-high formality")
    }
} 