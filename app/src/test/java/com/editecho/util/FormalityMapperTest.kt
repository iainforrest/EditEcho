package com.editecho.util

import com.editecho.prompt.ToneProfile
import com.editecho.prompt.VoiceDNA
import com.editecho.data.VoiceDNARepository
import com.editecho.util.FormalityMapper.FormalityBand
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import io.mockk.every
import io.mockk.mockk

/**
 * Comprehensive unit tests for FormalityMapper utility
 * Tests formality calculations, band classification, DNA selection logic, and edge cases
 */
class FormalityMapperTest {
    
    // ─── Formality Band Tests ──────────────────────────────────────────────
    
    @Test
    fun `should have exactly 5 formality bands`() {
        assertThat(FormalityBand.values()).hasLength(5)
    }
    
    @Test
    fun `formality bands should have correct ranges`() {
        assertThat(FormalityBand.LOW.range).isEqualTo(0..20)
        assertThat(FormalityBand.LOW_MID.range).isEqualTo(21..40)
        assertThat(FormalityBand.MID.range).isEqualTo(41..60)
        assertThat(FormalityBand.MID_HIGH.range).isEqualTo(61..80)
        assertThat(FormalityBand.HIGH.range).isEqualTo(81..100)
    }
    
    @Test
    fun `formality bands should have correct labels`() {
        assertThat(FormalityBand.LOW.label).isEqualTo("Low")
        assertThat(FormalityBand.LOW_MID.label).isEqualTo("Low-Mid")
        assertThat(FormalityBand.MID.label).isEqualTo("Mid")
        assertThat(FormalityBand.MID_HIGH.label).isEqualTo("Mid-High")
        assertThat(FormalityBand.HIGH.label).isEqualTo("High")
    }
    
    @Test
    fun `fromFormality should return correct bands`() {
        // Test boundary conditions
        assertThat(FormalityBand.fromFormality(0)).isEqualTo(FormalityBand.LOW)
        assertThat(FormalityBand.fromFormality(20)).isEqualTo(FormalityBand.LOW)
        assertThat(FormalityBand.fromFormality(21)).isEqualTo(FormalityBand.LOW_MID)
        assertThat(FormalityBand.fromFormality(40)).isEqualTo(FormalityBand.LOW_MID)
        assertThat(FormalityBand.fromFormality(41)).isEqualTo(FormalityBand.MID)
        assertThat(FormalityBand.fromFormality(60)).isEqualTo(FormalityBand.MID)
        assertThat(FormalityBand.fromFormality(61)).isEqualTo(FormalityBand.MID_HIGH)
        assertThat(FormalityBand.fromFormality(80)).isEqualTo(FormalityBand.MID_HIGH)
        assertThat(FormalityBand.fromFormality(81)).isEqualTo(FormalityBand.HIGH)
        assertThat(FormalityBand.fromFormality(100)).isEqualTo(FormalityBand.HIGH)
    }
    
    @Test
    fun `fromFormality should return null for invalid values`() {
        assertThat(FormalityBand.fromFormality(-1)).isNull()
        assertThat(FormalityBand.fromFormality(101)).isNull()
    }
    
    @Test
    fun `getAllLabels should return all band labels`() {
        val labels = FormalityBand.getAllLabels()
        assertThat(labels).containsExactly("Low", "Low-Mid", "Mid", "Mid-High", "High")
    }
    
    // ─── Formality Calculation Tests ───────────────────────────────────────
    
    @Test
    fun `calculateFormality should use ToneProfile calculation`() {
        // Test each tone profile
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, 0)).isEqualTo(10)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, 100)).isEqualTo(40)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, 50)).isEqualTo(25)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.NEUTRAL, 0)).isEqualTo(20)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.NEUTRAL, 100)).isEqualTo(70)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.NEUTRAL, 50)).isEqualTo(45)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.INFORMATIVE, 0)).isEqualTo(25)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.INFORMATIVE, 100)).isEqualTo(75)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.SUPPORTIVE, 0)).isEqualTo(20)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.SUPPORTIVE, 100)).isEqualTo(60)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.THOUGHTFUL, 0)).isEqualTo(30)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.THOUGHTFUL, 100)).isEqualTo(70)
    }
    
    @Test
    fun `calculateFormality with string should work for valid tones`() {
        assertThat(FormalityMapper.calculateFormality("Casual", 50)).isEqualTo(25)
        assertThat(FormalityMapper.calculateFormality("casual", 50)).isEqualTo(25) // case insensitive
        assertThat(FormalityMapper.calculateFormality("NEUTRAL", 50)).isEqualTo(45)
    }
    
    @Test
    fun `calculateFormality with string should return null for invalid tones`() {
        assertThat(FormalityMapper.calculateFormality("InvalidTone", 50)).isNull()
        assertThat(FormalityMapper.calculateFormality("", 50)).isNull()
    }
    
    @Test
    fun `calculateFormality should handle edge cases`() {
        // Test negative and over 100 polish levels
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, -10)).isEqualTo(10)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, 150)).isEqualTo(40)
    }
    
    // ─── Formality Band Classification Tests ───────────────────────────────
    
    @Test
    fun `getFormalityBand should classify formality levels correctly`() {
        assertThat(FormalityMapper.getFormalityBand(15)).isEqualTo(FormalityBand.LOW)
        assertThat(FormalityMapper.getFormalityBand(35)).isEqualTo(FormalityBand.LOW_MID)
        assertThat(FormalityMapper.getFormalityBand(50)).isEqualTo(FormalityBand.MID)
        assertThat(FormalityMapper.getFormalityBand(70)).isEqualTo(FormalityBand.MID_HIGH)
        assertThat(FormalityMapper.getFormalityBand(90)).isEqualTo(FormalityBand.HIGH)
    }
    
    @Test
    fun `getFormalityBand should return null for invalid levels`() {
        assertThat(FormalityMapper.getFormalityBand(-5)).isNull()
        assertThat(FormalityMapper.getFormalityBand(105)).isNull()
    }
    
    @Test
    fun `getFormalityBand with tone should work correctly`() {
        // Casual tone: 0% polish = 10% formality (LOW band)
        assertThat(FormalityMapper.getFormalityBand(ToneProfile.CASUAL, 0)).isEqualTo(FormalityBand.LOW)
        
        // Casual tone: 100% polish = 40% formality (LOW_MID band)
        assertThat(FormalityMapper.getFormalityBand(ToneProfile.CASUAL, 100)).isEqualTo(FormalityBand.LOW_MID)
        
        // Neutral tone: 100% polish = 70% formality (MID_HIGH band)
        assertThat(FormalityMapper.getFormalityBand(ToneProfile.NEUTRAL, 100)).isEqualTo(FormalityBand.MID_HIGH)
        
        // Informative tone: 100% polish = 75% formality (MID_HIGH band)
        assertThat(FormalityMapper.getFormalityBand(ToneProfile.INFORMATIVE, 100)).isEqualTo(FormalityBand.MID_HIGH)
    }
    
    // ─── DNA Selection Tests ───────────────────────────────────────────────
    
    @Test
    fun `selectDNA should use tone DNA when confidence is high`() {
        // Mock repository and DNA
        val mockRepository = mockk<VoiceDNARepository>()
        val highConfidenceDNA = VoiceDNA(
            tone = "Casual",
            confidence = 0.8f, // Above 0.7 threshold
            userFormalityRange = listOf(15, 25),
            theoreticalRange = listOf(10, 40),
            formalityShifts = "Test shifts",
            polishPatterns = "Test patterns",
            constants = "Test constants",
            voiceMarkers = "Test markers",
            antiPatterns = "Test anti-patterns",
            sourceExampleIds = listOf(1, 2, 3)
        )
        val formalityBandDNA = VoiceDNA(
            tone = "Low-Mid",
            confidence = 0.5f,
            userFormalityRange = listOf(25, 35),
            theoreticalRange = listOf(21, 40),
            formalityShifts = "Band shifts",
            polishPatterns = "Band patterns",
            constants = "Band constants",
            voiceMarkers = "Band markers",
            antiPatterns = "Band anti-patterns",
            sourceExampleIds = listOf(4, 5)
        )
        
        every { mockRepository.getToneDNA("Casual") } returns highConfidenceDNA
        every { mockRepository.getFormalityBandDNA(25) } returns formalityBandDNA
        
        val result = FormalityMapper.selectDNA(ToneProfile.CASUAL, 50, mockRepository)
        
        assertThat(result.primaryDNA).isEqualTo(highConfidenceDNA)
        assertThat(result.fallbackDNA).isEqualTo(formalityBandDNA)
        assertThat(result.useConfidenceThreshold).isTrue()
        assertThat(result.actualFormality).isEqualTo(25)
        assertThat(result.formalityBand).isEqualTo(FormalityBand.LOW_MID)
    }
    
    @Test
    fun `selectDNA should use formality band DNA when confidence is low`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val lowConfidenceDNA = VoiceDNA(
            tone = "Supportive",
            confidence = 0.3f, // Below 0.7 threshold
            userFormalityRange = listOf(20, 30),
            theoreticalRange = listOf(20, 60),
            formalityShifts = "Test shifts",
            polishPatterns = "Test patterns",
            constants = "Test constants",
            voiceMarkers = "Test markers",
            antiPatterns = "Test anti-patterns",
            sourceExampleIds = listOf(1, 2)
        )
        val formalityBandDNA = VoiceDNA(
            tone = "Low-Mid",
            confidence = 0.4f,
            userFormalityRange = listOf(25, 35),
            theoreticalRange = listOf(21, 40),
            formalityShifts = "Band shifts",
            polishPatterns = "Band patterns",
            constants = "Band constants",
            voiceMarkers = "Band markers",
            antiPatterns = "Band anti-patterns",
            sourceExampleIds = listOf(3, 4)
        )
        
        every { mockRepository.getToneDNA("Supportive") } returns lowConfidenceDNA
        every { mockRepository.getFormalityBandDNA(40) } returns formalityBandDNA
        
        val result = FormalityMapper.selectDNA(ToneProfile.SUPPORTIVE, 50, mockRepository)
        
        assertThat(result.primaryDNA).isEqualTo(formalityBandDNA)
        assertThat(result.fallbackDNA).isEqualTo(lowConfidenceDNA)
        assertThat(result.useConfidenceThreshold).isFalse()
        assertThat(result.actualFormality).isEqualTo(40) // Supportive 50% polish
        assertThat(result.formalityBand).isEqualTo(FormalityBand.LOW_MID)
    }
    
    @Test
    fun `selectDNA should handle missing tone DNA gracefully`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val formalityBandDNA = VoiceDNA(
            tone = "Mid",
            confidence = 0.4f,
            userFormalityRange = listOf(45, 55),
            theoreticalRange = listOf(41, 60),
            formalityShifts = "Band shifts",
            polishPatterns = "Band patterns",
            constants = "Band constants",
            voiceMarkers = "Band markers",
            antiPatterns = "Band anti-patterns",
            sourceExampleIds = listOf(3, 4)
        )
        
        every { mockRepository.getToneDNA("Neutral") } returns null
        every { mockRepository.getFormalityBandDNA(45) } returns formalityBandDNA
        
        val result = FormalityMapper.selectDNA(ToneProfile.NEUTRAL, 50, mockRepository)
        
        assertThat(result.primaryDNA).isEqualTo(formalityBandDNA)
        assertThat(result.fallbackDNA).isNull()
        assertThat(result.useConfidenceThreshold).isFalse()
    }
    
    @Test
    fun `selectDNA with tone name should work for valid tones`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val toneDNA = VoiceDNA(
            tone = "Thoughtful",
            confidence = 0.9f,
            userFormalityRange = listOf(30, 50),
            theoreticalRange = listOf(30, 70),
            formalityShifts = "Test shifts",
            polishPatterns = "Test patterns",
            constants = "Test constants",
            voiceMarkers = "Test markers",
            antiPatterns = "Test anti-patterns",
            sourceExampleIds = listOf(1)
        )
        
        every { mockRepository.getToneDNA("Thoughtful") } returns toneDNA
        
        val result = FormalityMapper.selectDNA("Thoughtful", 75, mockRepository)
        
        assertThat(result).isNotNull()
        assertThat(result!!.primaryDNA).isEqualTo(toneDNA)
        assertThat(result.useConfidenceThreshold).isTrue()
    }
    
    @Test
    fun `selectDNA with invalid tone name should return null`() {
        val mockRepository = mockk<VoiceDNARepository>()
        
        val result = FormalityMapper.selectDNA("InvalidTone", 50, mockRepository)
        
        assertThat(result).isNull()
    }
    
    @Test
    fun `selectDNA should use custom confidence threshold`() {
        val mockRepository = mockk<VoiceDNARepository>()
        val mediumConfidenceDNA = VoiceDNA(
            tone = "Neutral",
            confidence = 0.6f, // Below 0.7 but above 0.5
            userFormalityRange = listOf(30, 50),
            theoreticalRange = listOf(20, 70),
            formalityShifts = "Test shifts",
            polishPatterns = "Test patterns",
            constants = "Test constants",
            voiceMarkers = "Test markers",
            antiPatterns = "Test anti-patterns",
            sourceExampleIds = listOf(1, 2)
        )
        
        every { mockRepository.getToneDNA("Neutral") } returns mediumConfidenceDNA
        
        // With default threshold (0.7): should use formality band DNA
        val resultDefault = FormalityMapper.selectDNA(ToneProfile.NEUTRAL, 50, mockRepository)
        assertThat(resultDefault.useConfidenceThreshold).isFalse()
        
        // With custom threshold (0.5): should use tone DNA
        val resultCustom = FormalityMapper.selectDNA(ToneProfile.NEUTRAL, 50, mockRepository, 0.5f)
        assertThat(resultCustom.useConfidenceThreshold).isTrue()
        assertThat(resultCustom.primaryDNA).isEqualTo(mediumConfidenceDNA)
    }
    
    // ─── Utility Function Tests ────────────────────────────────────────────
    
    @Test
    fun `getAllToneFormalityRanges should return all tone ranges`() {
        val ranges = FormalityMapper.getAllToneFormalityRanges()
        
        assertThat(ranges).hasSize(5)
        assertThat(ranges["Casual"]).isEqualTo(Pair(10, 40))
        assertThat(ranges["Neutral"]).isEqualTo(Pair(20, 70))
        assertThat(ranges["Informative"]).isEqualTo(Pair(25, 75))
        assertThat(ranges["Supportive"]).isEqualTo(Pair(20, 60))
        assertThat(ranges["Thoughtful"]).isEqualTo(Pair(30, 70))
    }
    
    @Test
    fun `isValidCombination should return true for all valid polish levels`() {
        ToneProfile.values().forEach { tone ->
            // Test edge cases
            assertThat(FormalityMapper.isValidCombination(tone, 0)).isTrue()
            assertThat(FormalityMapper.isValidCombination(tone, 100)).isTrue()
            assertThat(FormalityMapper.isValidCombination(tone, 50)).isTrue()
            
            // Test out of bounds (should still be true due to clamping)
            assertThat(FormalityMapper.isValidCombination(tone, -10)).isTrue()
            assertThat(FormalityMapper.isValidCombination(tone, 150)).isTrue()
        }
    }
    
    @Test
    fun `getFormalityProgression should return correct progression`() {
        val progression = FormalityMapper.getFormalityProgression(ToneProfile.CASUAL, 5)
        
        assertThat(progression).hasSize(5)
        assertThat(progression[0]).isEqualTo(Pair(0, 10))    // 0% polish = 10% formality
        assertThat(progression[1]).isEqualTo(Pair(25, 17))   // 25% polish = 17% formality  
        assertThat(progression[2]).isEqualTo(Pair(50, 25))   // 50% polish = 25% formality
        assertThat(progression[3]).isEqualTo(Pair(75, 32))   // 75% polish = 32% formality
        assertThat(progression[4]).isEqualTo(Pair(100, 40))  // 100% polish = 40% formality
    }
    
    @Test
    fun `getFormalityProgression should handle edge cases`() {
        // Minimum steps
        val minProgression = FormalityMapper.getFormalityProgression(ToneProfile.NEUTRAL, 2)
        assertThat(minProgression).hasSize(2)
        assertThat(minProgression[0]).isEqualTo(Pair(0, 20))
        assertThat(minProgression[1]).isEqualTo(Pair(100, 70))
    }
    
    @Test
    fun `getFormalityProgression should fail for invalid steps`() {
        try {
            FormalityMapper.getFormalityProgression(ToneProfile.CASUAL, 1)
            assertThat(false).isTrue() // Should have thrown exception
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("Need at least 2 steps")
        }
    }
    
    @Test
    fun `getFormalityContext should return appropriate descriptions`() {
        assertThat(FormalityMapper.getFormalityContext(10)).isEqualTo("Text fragments, minimal grammar")
        assertThat(FormalityMapper.getFormalityContext(25)).isEqualTo("Casual digital communication")
        assertThat(FormalityMapper.getFormalityContext(35)).isEqualTo("Friendly personal messages")
        assertThat(FormalityMapper.getFormalityContext(45)).isEqualTo("Relaxed professional communication")
        assertThat(FormalityMapper.getFormalityContext(55)).isEqualTo("Standard business communication")
        assertThat(FormalityMapper.getFormalityContext(65)).isEqualTo("Polished professional writing")
        assertThat(FormalityMapper.getFormalityContext(75)).isEqualTo("Formal business communication")
        assertThat(FormalityMapper.getFormalityContext(85)).isEqualTo("Highly formal, ceremonial language")
        assertThat(FormalityMapper.getFormalityContext(95)).isEqualTo("Academic, legal, or archival formality")
        assertThat(FormalityMapper.getFormalityContext(150)).isEqualTo("Invalid formality level")
    }
    
    // ─── PRD Compliance Tests ──────────────────────────────────────────────
    
    @Test
    fun `should comply with PRD formality ranges`() {
        // Test exact PRD requirements from R3.2.1
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, 0)).isEqualTo(10)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.CASUAL, 100)).isEqualTo(40)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.NEUTRAL, 0)).isEqualTo(20)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.NEUTRAL, 100)).isEqualTo(70)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.INFORMATIVE, 0)).isEqualTo(25)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.INFORMATIVE, 100)).isEqualTo(75)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.SUPPORTIVE, 0)).isEqualTo(20)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.SUPPORTIVE, 100)).isEqualTo(60)
        
        assertThat(FormalityMapper.calculateFormality(ToneProfile.THOUGHTFUL, 0)).isEqualTo(30)
        assertThat(FormalityMapper.calculateFormality(ToneProfile.THOUGHTFUL, 100)).isEqualTo(70)
    }
    
    @Test
    fun `should comply with PRD formality band ranges`() {
        // Test exact PRD requirements from R3.2.3
        assertThat(FormalityBand.LOW.range).isEqualTo(0..20)
        assertThat(FormalityBand.LOW_MID.range).isEqualTo(21..40)
        assertThat(FormalityBand.MID.range).isEqualTo(41..60)
        assertThat(FormalityBand.MID_HIGH.range).isEqualTo(61..80)
        assertThat(FormalityBand.HIGH.range).isEqualTo(81..100)
    }
    
    @Test
    fun `should comply with PRD confidence threshold requirement`() {
        // Test exact PRD requirement from R3.3.1: ≥0.7 confidence = tone DNA, <0.7 = formality band DNA
        val mockRepository = mockk<VoiceDNARepository>()
        
        // Test exact threshold boundary
        val exactThresholdDNA = VoiceDNA(
            tone = "Neutral",
            confidence = 0.7f, // Exactly at threshold
            userFormalityRange = listOf(30, 50),
            theoreticalRange = listOf(20, 70),
            formalityShifts = "Test shifts",
            polishPatterns = "Test patterns",
            constants = "Test constants",
            voiceMarkers = "Test markers",
            antiPatterns = "Test anti-patterns",
            sourceExampleIds = listOf(1)
        )
        
        every { mockRepository.getToneDNA("Neutral") } returns exactThresholdDNA
        
        val result = FormalityMapper.selectDNA(ToneProfile.NEUTRAL, 50, mockRepository)
        
        // Should use tone DNA as primary (≥0.7)
        assertThat(result.useConfidenceThreshold).isTrue()
        assertThat(result.primaryDNA).isEqualTo(exactThresholdDNA)
    }
} 