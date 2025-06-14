package com.editecho.util

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility class for converting raw PCM audio chunks to WAV format.
 * This is needed for fallback compatibility with Deepgram's batch API.
 */
class AudioFormatConverter {
    
    companion object {
        private const val TAG = "AudioFormatConverter"
        
        // WAV format constants
        private const val WAV_HEADER_SIZE = 44
        private const val BITS_PER_SAMPLE = 16
        private const val BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8
    }
    
    /**
     * Convert raw PCM chunks to a WAV file.
     * 
     * @param pcmChunks List of raw PCM audio chunks
     * @param outputFile Output WAV file
     * @param sampleRate Audio sample rate (e.g., 16000)
     * @param channels Number of audio channels (1 for mono, 2 for stereo)
     * @throws IOException if file operations fail
     */
    @Throws(IOException::class)
    fun convertPcmToWav(
        pcmChunks: List<ByteArray>,
        outputFile: File,
        sampleRate: Int,
        channels: Int
    ) {
        try {
            // Calculate total PCM data size
            val totalPcmSize = pcmChunks.sumOf { it.size }
            
            Log.d(TAG, "Converting ${pcmChunks.size} PCM chunks to WAV")
            Log.d(TAG, "Total PCM size: $totalPcmSize bytes, Sample rate: ${sampleRate}Hz, Channels: $channels")
            
            FileOutputStream(outputFile).use { fos ->
                // Write WAV header
                writeWavHeader(fos, totalPcmSize, sampleRate, channels)
                
                // Write PCM data chunks
                for (chunk in pcmChunks) {
                    fos.write(chunk)
                }
                
                fos.flush()
            }
            
            Log.d(TAG, "Successfully converted PCM to WAV: ${outputFile.absolutePath}")
            Log.d(TAG, "WAV file size: ${outputFile.length()} bytes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert PCM to WAV", e)
            throw IOException("PCM to WAV conversion failed: ${e.message}")
        }
    }
    
    /**
     * Write WAV file header with proper format specifications.
     */
    private fun writeWavHeader(
        fos: FileOutputStream,
        pcmDataSize: Int,
        sampleRate: Int,
        channels: Int
    ) {
        val byteRate = sampleRate * channels * BYTES_PER_SAMPLE
        val blockAlign = channels * BYTES_PER_SAMPLE
        val totalFileSize = WAV_HEADER_SIZE + pcmDataSize - 8 // Subtract 8 for RIFF header
        
        val header = ByteBuffer.allocate(WAV_HEADER_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            
            // RIFF chunk descriptor
            put("RIFF".toByteArray()) // ChunkID
            putInt(totalFileSize) // ChunkSize
            put("WAVE".toByteArray()) // Format
            
            // fmt sub-chunk
            put("fmt ".toByteArray()) // Subchunk1ID
            putInt(16) // Subchunk1Size (16 for PCM)
            putShort(1) // AudioFormat (1 for PCM)
            putShort(channels.toShort()) // NumChannels
            putInt(sampleRate) // SampleRate
            putInt(byteRate) // ByteRate
            putShort(blockAlign.toShort()) // BlockAlign
            putShort(BITS_PER_SAMPLE.toShort()) // BitsPerSample
            
            // data sub-chunk
            put("data".toByteArray()) // Subchunk2ID
            putInt(pcmDataSize) // Subchunk2Size
        }
        
        fos.write(header.array())
        
        Log.d(TAG, "WAV header written - Sample rate: ${sampleRate}Hz, Channels: $channels, Data size: $pcmDataSize bytes")
    }
    
    /**
     * Create a WAV file from a single PCM byte array.
     * Convenience method for single-chunk conversion.
     */
    @Throws(IOException::class)
    fun convertSinglePcmToWav(
        pcmData: ByteArray,
        outputFile: File,
        sampleRate: Int,
        channels: Int
    ) {
        convertPcmToWav(listOf(pcmData), outputFile, sampleRate, channels)
    }
    
    /**
     * Validate audio parameters for WAV conversion.
     */
    fun validateAudioParameters(sampleRate: Int, channels: Int): Boolean {
        return when {
            sampleRate <= 0 -> {
                Log.e(TAG, "Invalid sample rate: $sampleRate")
                false
            }
            channels !in 1..2 -> {
                Log.e(TAG, "Invalid channel count: $channels (must be 1 or 2)")
                false
            }
            else -> {
                Log.d(TAG, "Audio parameters validated: ${sampleRate}Hz, ${channels}ch")
                true
            }
        }
    }
    
    /**
     * Calculate the duration of PCM data in milliseconds.
     */
    fun calculateDurationMs(pcmDataSize: Int, sampleRate: Int, channels: Int): Long {
        val samplesPerChannel = pcmDataSize / (channels * BYTES_PER_SAMPLE)
        return (samplesPerChannel * 1000L) / sampleRate
    }
} 