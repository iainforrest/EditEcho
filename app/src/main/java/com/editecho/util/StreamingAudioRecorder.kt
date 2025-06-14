package com.editecho.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Streaming audio recorder that captures raw audio chunks for real-time processing.
 * Unlike MediaRecorder which saves to a file, this captures raw PCM data that can be
 * streamed to services like Deepgram in real-time.
 */
class StreamingAudioRecorder {
    companion object {
        private const val TAG = "StreamingAudioRecorder"
        
        // Audio configuration for Deepgram compatibility
        private const val SAMPLE_RATE = 16000 // 16kHz is optimal for speech recognition
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_MULTIPLIER = 4 // Larger buffer for stability
    }
    
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false
    
    // For fallback - save audio chunks to a file simultaneously
    private var fallbackFile: File? = null
    private var fallbackOutputStream: FileOutputStream? = null
    
    // Collect PCM chunks for WAV conversion in case of fallback
    private val pcmChunks = mutableListOf<ByteArray>()
    private var audioConfig: AudioConfig? = null
    
    // Flow for streaming audio chunks to consumers (like DeepgramRepository)
    private val _audioChunks = MutableSharedFlow<ByteArray>(
        extraBufferCapacity = 100 // Buffer up to 100 chunks if consumer is slow
    )
    val audioChunks: SharedFlow<ByteArray> = _audioChunks.asSharedFlow()
    
    // Calculate buffer size
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT
    ) * BUFFER_SIZE_MULTIPLIER
    
    /**
     * Starts recording audio and streaming chunks.
     * Also saves chunks to a fallback file for error recovery.
     * 
     * @param fallbackFile File to save audio chunks for fallback transcription
     * @throws IOException if recording cannot be started
     */
    @Throws(IOException::class)
    fun startRecording(fallbackFile: File) {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress")
            return
        }
        
        try {
            this.fallbackFile = fallbackFile
            this.fallbackOutputStream = FileOutputStream(fallbackFile)
            
            // Initialize audio configuration and clear previous chunks
            audioConfig = getAudioConfig()
            pcmChunks.clear()
            
            // Initialize AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            // Check if AudioRecord was initialized successfully
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IOException("AudioRecord initialization failed")
            }
            
            // Start recording
            audioRecord?.startRecording()
            isRecording = true
            
            // Start the recording coroutine
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                recordAudioChunks()
            }
            
            Log.d(TAG, "Streaming audio recording started - Sample Rate: $SAMPLE_RATE Hz, Buffer Size: $bufferSize bytes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            cleanup()
            throw IOException("Failed to start recording: ${e.message}")
        }
    }
    
    /**
     * Stops recording and returns the fallback file.
     * 
     * @return The fallback audio file containing all recorded chunks
     * @throws IOException if there's an error stopping the recording
     */
    @Throws(IOException::class)
    fun stopRecording(): File {
        if (!isRecording) {
            throw IOException("No recording in progress")
        }
        
        try {
            isRecording = false
            
            // Cancel the recording job
            recordingJob?.cancel()
            recordingJob = null
            
            // Stop AudioRecord
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
            audioRecord = null
            
            // Close fallback file
            fallbackOutputStream?.close()
            fallbackOutputStream = null
            
            Log.d(TAG, "Streaming audio recording stopped")
            
            return fallbackFile ?: throw IOException("No fallback file was created")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            cleanup()
            throw IOException("Failed to stop recording: ${e.message}")
        }
    }
    
    /**
     * Main recording loop that captures audio chunks and emits them.
     */
    private suspend fun recordAudioChunks() {
        val buffer = ByteArray(bufferSize)
        
        while (currentCoroutineContext().isActive && isRecording) {
            try {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                
                if (bytesRead > 0) {
                    // Create a copy of the buffer with only the bytes that were read
                    val audioChunk = buffer.copyOf(bytesRead)
                    
                    // Collect chunk for WAV conversion
                    pcmChunks.add(audioChunk.clone())
                    
                    // Emit to streaming flow
                    _audioChunks.tryEmit(audioChunk)
                    
                    // Save to fallback file (raw PCM)
                    fallbackOutputStream?.write(audioChunk)
                    
                } else if (bytesRead < 0) {
                    Log.e(TAG, "AudioRecord read error: $bytesRead")
                    break
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error reading audio data", e)
                break
            }
        }
        
        Log.d(TAG, "Audio recording loop ended")
    }
    
    /**
     * Clean up resources in case of error.
     */
    private fun cleanup() {
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        
        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        }
        audioRecord = null
        
        fallbackOutputStream?.close()
        fallbackOutputStream = null
    }
    
    /**
     * Get audio configuration details for consumers.
     */
    fun getAudioConfig(): AudioConfig {
        return AudioConfig(
            sampleRate = SAMPLE_RATE,
            channels = 1, // Mono
            bitsPerSample = 16,
            encoding = "linear16" // Deepgram format
        )
    }
    
    /**
     * Get the collected PCM chunks for WAV conversion.
     * 
     * @return List of PCM audio chunks
     */
    fun getPcmChunks(): List<ByteArray> {
        return pcmChunks.toList() // Return a copy to prevent modification
    }
    
    /**
     * Create a WAV file from the collected PCM chunks.
     * 
     * @param wavFile Output WAV file
     * @return true if WAV file was created successfully, false otherwise
     */
    fun createWavFile(wavFile: File): Boolean {
        return try {
            val config = audioConfig ?: throw IllegalStateException("Audio configuration not available")
            val converter = AudioFormatConverter()
            
            if (!converter.validateAudioParameters(config.sampleRate, config.channels)) {
                throw IllegalArgumentException("Invalid audio parameters")
            }
            
            converter.convertPcmToWav(
                pcmChunks = pcmChunks,
                outputFile = wavFile,
                sampleRate = config.sampleRate,
                channels = config.channels
            )
            
            Log.d(TAG, "WAV file created successfully: ${wavFile.absolutePath}")
            Log.d(TAG, "WAV file size: ${wavFile.length()} bytes")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create WAV file", e)
            false
        }
    }
    
    /**
     * Get the duration of recorded audio in milliseconds.
     */
    fun getRecordingDurationMs(): Long {
        val config = audioConfig ?: return 0L
        val totalPcmSize = pcmChunks.sumOf { it.size }
        return AudioFormatConverter().calculateDurationMs(totalPcmSize, config.sampleRate, config.channels)
    }
    
    /**
     * Data class to hold audio configuration information.
     */
    data class AudioConfig(
        val sampleRate: Int,
        val channels: Int,
        val bitsPerSample: Int,
        val encoding: String
    )
} 