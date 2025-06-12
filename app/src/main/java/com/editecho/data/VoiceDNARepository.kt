package com.editecho.data

import android.content.Context
import android.util.Log
import com.editecho.prompt.VoiceDNA
import com.editecho.prompt.VoiceDNACollection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for loading and managing Voice DNA patterns from JSON assets
 * Supports Voice Engine 3.0 with tone-specific and formality band DNA patterns
 */
@Singleton
class VoiceDNARepository @Inject constructor(@ApplicationContext private val context: Context) {
    
    companion object {
        private const val TAG = "VoiceDNARepository"
        private const val DNA_PATTERNS_FILE = "voice_dna_patterns"
        
        // Confidence threshold for DNA selection (from PRD)
        const val CONFIDENCE_THRESHOLD = 0.7f
    }
    
    private var _voiceDNACollection: VoiceDNACollection? = null
    private val gson = Gson()
    
    /**
     * Load Voice DNA patterns from JSON assets
     * @return VoiceDNACollection or null if loading fails
     */
    fun loadVoiceDNAPatterns(): VoiceDNACollection? {
        if (_voiceDNACollection != null) {
            return _voiceDNACollection
        }
        
        try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier(DNA_PATTERNS_FILE, "raw", context.packageName)
            )
            
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<VoiceDNACollection>() {}.type
            _voiceDNACollection = gson.fromJson(reader, type)
            
            reader.close()
            inputStream.close()
            
            Log.d(TAG, "Successfully loaded ${_voiceDNACollection?.toneSpecificDNA?.size ?: 0} tone-specific DNA patterns")
            Log.d(TAG, "Successfully loaded ${_voiceDNACollection?.formalityBandDNA?.size ?: 0} formality band DNA patterns")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Voice DNA patterns", e)
            _voiceDNACollection = null
        }
        
        return _voiceDNACollection
    }
    
    /**
     * Get tone-specific DNA by tone name
     * @param tone The tone name (e.g., "Casual", "Neutral", etc.)
     * @return VoiceDNA pattern or null if not found
     */
    fun getToneDNA(tone: String): VoiceDNA? {
        val collection = loadVoiceDNAPatterns() ?: return null
        return collection.getToneDNA(tone)
    }
    
    /**
     * Get formality band DNA by formality level
     * @param formalityLevel The formality level (0-100)
     * @return VoiceDNA pattern or null if not found
     */
    fun getFormalityBandDNA(formalityLevel: Int): VoiceDNA? {
        val collection = loadVoiceDNAPatterns() ?: return null
        return collection.getFormalityBandDNA(formalityLevel)
    }
    
    /**
     * Get all available tone names
     * @return List of tone names or empty list if loading fails
     */
    fun getAvailableTones(): List<String> {
        val collection = loadVoiceDNAPatterns() ?: return emptyList()
        return collection.getAvailableTones()
    }
    
    /**
     * Get appropriate DNA pattern based on confidence logic from PRD
     * @param selectedTone The user-selected tone
     * @param formalityLevel The calculated formality level
     * @return Pair of (primary DNA, fallback DNA or null)
     */
    fun getDNAForPrompt(selectedTone: String, formalityLevel: Int): Pair<VoiceDNA?, VoiceDNA?> {
        val toneDNA = getToneDNA(selectedTone)
        
        // High confidence (≥0.7): Use tone DNA with formality guidance
        if (toneDNA != null && toneDNA.confidence >= CONFIDENCE_THRESHOLD) {
            Log.d(TAG, "High confidence (${toneDNA.confidence}) - using tone DNA for $selectedTone")
            return Pair(toneDNA, null)
        }
        
        // Low confidence (<0.7): Use formality band DNA with tone intent
        val formalityBandDNA = getFormalityBandDNA(formalityLevel)
        if (formalityBandDNA != null) {
            Log.d(TAG, "Low confidence (${toneDNA?.confidence ?: 0.0}) - using formality band DNA for level $formalityLevel")
            return Pair(formalityBandDNA, toneDNA)
        }
        
        Log.w(TAG, "No suitable DNA pattern found for tone: $selectedTone, formality: $formalityLevel")
        return Pair(toneDNA, null) // Fallback to tone DNA even if low confidence
    }
    
    /**
     * Check if Voice DNA patterns are loaded and available
     * @return true if patterns are loaded, false otherwise
     */
    fun isLoaded(): Boolean {
        return _voiceDNACollection != null
    }
    
    /**
     * Get statistics about loaded DNA patterns
     * @return Map of pattern statistics or empty map if not loaded
     */
    fun getStatistics(): Map<String, Any> {
        val collection = _voiceDNACollection ?: return emptyMap()
        
        val toneConfidences = collection.toneSpecificDNA.associate { 
            it.tone to it.confidence 
        }
        
        val formalityRanges = collection.formalityBandDNA.associate {
            it.tone to "${it.theoreticalRange.firstOrNull()}-${it.theoreticalRange.lastOrNull()}%"
        }
        
        return mapOf(
            "toneCount" to collection.toneSpecificDNA.size,
            "formalityBandCount" to collection.formalityBandDNA.size,
            "toneConfidences" to toneConfidences,
            "formalityRanges" to formalityRanges,
            "highConfidenceTones" to collection.toneSpecificDNA.filter { 
                it.confidence >= CONFIDENCE_THRESHOLD 
            }.map { it.tone },
            "lowConfidenceTones" to collection.toneSpecificDNA.filter { 
                it.confidence < CONFIDENCE_THRESHOLD 
            }.map { it.tone }
        )
    }
    
    /**
     * Clear cached DNA patterns (useful for testing or memory management)
     */
    fun clearCache() {
        _voiceDNACollection = null
        Log.d(TAG, "Voice DNA patterns cache cleared")
    }
} 