package com.editecho.prompt

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for ToneProfile enum functionality
 * Tests formality calculations, micro-labels, and Voice Engine 3.0 features
 */
class ToneProfileTest {
    
    // ─── Basic Enum Structure Tests ────────────────────────────────────────
    
    @Test
    fun `should have exactly 5 tone profiles`() {
        assertThat(ToneProfile.values()).hasLength(5)
    }
    
    @Test
    fun `should have all required tone profiles`() {
        val toneNames = ToneProfile.values().map { it.displayName }
        assertThat(toneNames).containsExactly(
            "Casual", "Neutral", "Informative", "Supportive", "Thoughtful"
        )
    }
    
    @Test
    fun `all tone profiles should have valid display names`() {
        ToneProfile.values().forEach { tone ->
            assertThat(tone.displayName).isNotEmpty()
        }
    }
    
    @Test
    fun `all tone profiles should have descriptions`() {
        ToneProfile.values().forEach { tone ->
            assertThat(tone.description).isNotEmpty()
        }
    }
    
    // ─── Formality Range Tests ─────────────────────────────────────────────
    
    @Test
    fun `Casual should have correct formality range`() {
        assertThat(ToneProfile.CASUAL.minFormality).isEqualTo(10)
        assertThat(ToneProfile.CASUAL.maxFormality).isEqualTo(40)
    }
    
    @Test
    fun `Neutral should have correct formality range`() {
        assertThat(ToneProfile.NEUTRAL.minFormality).isEqualTo(20)
        assertThat(ToneProfile.NEUTRAL.maxFormality).isEqualTo(70)
    }
    
    @Test
    fun `Informative should have correct formality range`() {
        assertThat(ToneProfile.INFORMATIVE.minFormality).isEqualTo(25)
        assertThat(ToneProfile.INFORMATIVE.maxFormality).isEqualTo(75)
    }
    
    @Test
    fun `Supportive should have correct formality range`() {
        assertThat(ToneProfile.SUPPORTIVE.minFormality).isEqualTo(20)
        assertThat(ToneProfile.SUPPORTIVE.maxFormality).isEqualTo(60)
    }
    
    @Test
    fun `Thoughtful should have correct formality range`() {
        assertThat(ToneProfile.THOUGHTFUL.minFormality).isEqualTo(30)
        assertThat(ToneProfile.THOUGHTFUL.maxFormality).isEqualTo(70)
    }
    
    @Test
    fun `all formality ranges should be valid`() {
        ToneProfile.values().forEach { tone ->
            assertThat(tone.minFormality).isAtLeast(0)
            assertThat(tone.maxFormality).isAtMost(100)
            assertThat(tone.minFormality).isLessThan(tone.maxFormality)
        }
    }
    
    // ─── Micro-label Tests ─────────────────────────────────────────────────
    
    @Test
    fun `Casual should have correct micro-labels`() {
        assertThat(ToneProfile.CASUAL.lowMicroLabel).isEqualTo("relaxed")
        assertThat(ToneProfile.CASUAL.highMicroLabel).isEqualTo("clear")
    }
    
    @Test
    fun `Neutral should have correct micro-labels`() {
        assertThat(ToneProfile.NEUTRAL.lowMicroLabel).isEqualTo("simple")
        assertThat(ToneProfile.NEUTRAL.highMicroLabel).isEqualTo("structured")
    }
    
    @Test
    fun `Informative should have correct micro-labels`() {
        assertThat(ToneProfile.INFORMATIVE.lowMicroLabel).isEqualTo("empathetic")
        assertThat(ToneProfile.INFORMATIVE.highMicroLabel).isEqualTo("direct")
    }
    
    @Test
    fun `Supportive should have correct micro-labels`() {
        assertThat(ToneProfile.SUPPORTIVE.lowMicroLabel).isEqualTo("empathetic")
        assertThat(ToneProfile.SUPPORTIVE.highMicroLabel).isEqualTo("reassuring")
    }
    
    @Test
    fun `Thoughtful should have correct micro-labels`() {
        assertThat(ToneProfile.THOUGHTFUL.lowMicroLabel).isEqualTo("brief")
        assertThat(ToneProfile.THOUGHTFUL.highMicroLabel).isEqualTo("structured")
    }
    
    @Test
    fun `all micro-labels should be non-empty`() {
        ToneProfile.values().forEach { tone ->
            assertThat(tone.lowMicroLabel).isNotEmpty()
            assertThat(tone.highMicroLabel).isNotEmpty()
        }
    }
    
    // ─── Formality Calculation Tests ───────────────────────────────────────
    
    @Test
    fun `calculateFormality should work correctly for Casual tone`() {
        val casual = ToneProfile.CASUAL
        
        // Polish 0% should give min formality
        assertThat(casual.calculateFormality(0)).isEqualTo(10)
        
        // Polish 100% should give max formality
        assertThat(casual.calculateFormality(100)).isEqualTo(40)
        
        // Polish 50% should give middle formality
        assertThat(casual.calculateFormality(50)).isEqualTo(25)
    }
    
    @Test
    fun `calculateFormality should work correctly for Neutral tone`() {
        val neutral = ToneProfile.NEUTRAL
        
        assertThat(neutral.calculateFormality(0)).isEqualTo(20)
        assertThat(neutral.calculateFormality(100)).isEqualTo(70)
        assertThat(neutral.calculateFormality(50)).isEqualTo(45)
    }
    
    @Test
    fun `calculateFormality should handle edge cases`() {
        val casual = ToneProfile.CASUAL
        
        // Negative polish should be clamped to 0
        assertThat(casual.calculateFormality(-10)).isEqualTo(10)
        
        // Polish over 100 should be clamped to 100
        assertThat(casual.calculateFormality(150)).isEqualTo(40)
    }
    
    @Test
    fun `calculateFormality should produce values within formality range`() {
        ToneProfile.values().forEach { tone ->
            // Test various polish levels
            listOf(0, 25, 50, 75, 100).forEach { polishLevel ->
                val formality = tone.calculateFormality(polishLevel)
                assertThat(formality).isAtLeast(tone.minFormality)
                assertThat(formality).isAtMost(tone.maxFormality)
            }
        }
    }
    
    @Test
    fun `calculateFormality should be monotonic`() {
        ToneProfile.values().forEach { tone ->
            val formality25 = tone.calculateFormality(25)
            val formality50 = tone.calculateFormality(50)
            val formality75 = tone.calculateFormality(75)
            
            assertThat(formality25).isAtMost(formality50)
            assertThat(formality50).isAtMost(formality75)
        }
    }
    
    // ─── Helper Method Tests ────────────────────────────────────────────────
    
    @Test
    fun `getFormalityRange should return correct pair`() {
        val casual = ToneProfile.CASUAL
        val range = casual.getFormalityRange()
        
        assertThat(range.first).isEqualTo(10)
        assertThat(range.second).isEqualTo(40)
    }
    
    @Test
    fun `getMicroLabels should return correct pair`() {
        val casual = ToneProfile.CASUAL
        val labels = casual.getMicroLabels()
        
        assertThat(labels.first).isEqualTo("relaxed")
        assertThat(labels.second).isEqualTo("clear")
    }
    
    // ─── Companion Object Tests ────────────────────────────────────────────
    
    @Test
    fun `fromName should find tone by exact name`() {
        assertThat(ToneProfile.fromName("Casual")).isEqualTo(ToneProfile.CASUAL)
        assertThat(ToneProfile.fromName("Neutral")).isEqualTo(ToneProfile.NEUTRAL)
        assertThat(ToneProfile.fromName("Informative")).isEqualTo(ToneProfile.INFORMATIVE)
        assertThat(ToneProfile.fromName("Supportive")).isEqualTo(ToneProfile.SUPPORTIVE)
        assertThat(ToneProfile.fromName("Thoughtful")).isEqualTo(ToneProfile.THOUGHTFUL)
    }
    
    @Test
    fun `fromName should be case insensitive`() {
        assertThat(ToneProfile.fromName("casual")).isEqualTo(ToneProfile.CASUAL)
        assertThat(ToneProfile.fromName("NEUTRAL")).isEqualTo(ToneProfile.NEUTRAL)
        assertThat(ToneProfile.fromName("InFoRmAtIvE")).isEqualTo(ToneProfile.INFORMATIVE)
    }
    
    @Test
    fun `fromName should return null for invalid names`() {
        assertThat(ToneProfile.fromName("InvalidTone")).isNull()
        assertThat(ToneProfile.fromName("")).isNull()
        assertThat(ToneProfile.fromName("Friendly")).isNull() // Old Voice Engine 2.0 tone
    }
    
    @Test
    fun `getDefault should return Neutral`() {
        assertThat(ToneProfile.getDefault()).isEqualTo(ToneProfile.NEUTRAL)
    }
    
    @Test
    fun `getAllToneNames should return all display names`() {
        val toneNames = ToneProfile.getAllToneNames()
        
        assertThat(toneNames).hasSize(5)
        assertThat(toneNames).containsExactly(
            "Casual", "Neutral", "Informative", "Supportive", "Thoughtful"
        )
    }
    
    @Test
    fun `toRepositoryFormat should return display name`() {
        assertThat(ToneProfile.CASUAL.toRepositoryFormat()).isEqualTo("Casual")
        assertThat(ToneProfile.NEUTRAL.toRepositoryFormat()).isEqualTo("Neutral")
    }
    
    // ─── Voice Engine 3.0 Integration Tests ────────────────────────────────
    
    @Test
    fun `tone profiles should match VoiceDNA repository expectations`() {
        // These tone names should match the ones in our VoiceDNA JSON
        val expectedTones = listOf("Casual", "Neutral", "Informative", "Supportive", "Thoughtful")
        val actualTones = ToneProfile.getAllToneNames()
        
        assertThat(actualTones).containsExactlyElementsIn(expectedTones)
    }
    
    @Test
    fun `formality ranges should align with PRD specifications`() {
        // Verify that our formality ranges match the PRD requirements
        assertThat(ToneProfile.CASUAL.getFormalityRange()).isEqualTo(Pair(10, 40))
        assertThat(ToneProfile.NEUTRAL.getFormalityRange()).isEqualTo(Pair(20, 70))
        assertThat(ToneProfile.INFORMATIVE.getFormalityRange()).isEqualTo(Pair(25, 75))
        assertThat(ToneProfile.SUPPORTIVE.getFormalityRange()).isEqualTo(Pair(20, 60))
        assertThat(ToneProfile.THOUGHTFUL.getFormalityRange()).isEqualTo(Pair(30, 70))
    }
    
    @Test
    fun `micro-labels should match voice engine guide specifications`() {
        // Verify micro-labels match the Voice Engine 3.0 guide
        val expectedLabels = mapOf(
            ToneProfile.CASUAL to Pair("relaxed", "clear"),
            ToneProfile.NEUTRAL to Pair("simple", "structured"), 
            ToneProfile.INFORMATIVE to Pair("empathetic", "direct"),
            ToneProfile.SUPPORTIVE to Pair("empathetic", "reassuring"),
            ToneProfile.THOUGHTFUL to Pair("brief", "structured")
        )
        
        expectedLabels.forEach { (tone, expectedPair) ->
            assertThat(tone.getMicroLabels()).isEqualTo(expectedPair)
        }
    }
} 