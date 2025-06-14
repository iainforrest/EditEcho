package com.editecho.network

import android.util.Log
import com.editecho.BuildConfig
import com.editecho.network.dto.DeepgramResponse
import com.editecho.network.dto.getTranscript
import com.editecho.network.dto.isTranscriptionResult
import com.editecho.network.dto.isInterimResult
import com.editecho.network.dto.isFinalResult
import com.editecho.network.dto.isSpeechFinalResult
import com.editecho.util.StreamingAudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling real-time transcription with Deepgram's streaming API.
 * 
 * This repository manages WebSocket connections to Deepgram, handles audio streaming,
 * processes transcription results, and provides fallback mechanisms for network failures.
 */
@Singleton
class DeepgramRepository @Inject constructor() {
    
    companion object {
        private const val TAG = "DeepgramRepository"
        private const val DEEPGRAM_WS_URL = "wss://api.deepgram.com/v1/listen"
        
        // Hardcoded keywords as specified in the PRD
        private val KEYWORDS = listOf(
            "Aleisha" to 1.2,
            "Te Anau" to 1.2,
            "Iain Forrest" to 1.2
        )
        
        // WebSocket connection timeout
        private const val CONNECTION_TIMEOUT_SECONDS = 30L
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // No read timeout for streaming
        .writeTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    private var webSocket: WebSocket? = null
    private var isConnected = false
    
    // Audio streaming integration
    private var streamingJob: Job? = null
    private var audioRecorder: StreamingAudioRecorder? = null
    
    // KeepAlive mechanism to prevent 10-second timeout
    private var keepAliveJob: Job? = null
    private var lastAudioSentTime = 0L
    private val keepAliveIntervalMs = 8000L // Send keepalive every 8 seconds
    private val audioTimeoutMs = 10000L // Deepgram timeout is ~10 seconds
    
    // State flows for connection status and transcription results
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _transcriptionResult = MutableStateFlow("")
    val transcriptionResult: StateFlow<String> = _transcriptionResult.asStateFlow()
    
    // Accumulated transcript from partial results
    private val transcriptBuilder = StringBuilder()
    
    // Transcript accumulation for real-time streaming
    private val interimTranscriptBuilder = StringBuilder()
    private val finalTranscriptBuilder = StringBuilder()
    private var lastInterimLength = 0
    
    // State for tracking transcription progress
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()
    
    private val _hasReceivedFirstResult = MutableStateFlow(false)
    val hasReceivedFirstResult: StateFlow<Boolean> = _hasReceivedFirstResult.asStateFlow()
    
    /**
     * Connection states for the WebSocket
     */
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }
    
    /**
     * Initialize a WebSocket connection to Deepgram's streaming endpoint.
     * 
     * @param sampleRate Audio sample rate (e.g., 16000)
     * @param channels Number of audio channels (1 for mono)
     * @param encoding Audio encoding format (e.g., "linear16")
     * @return true if connection was initiated successfully, false otherwise
     */
    fun initializeWebSocketConnection(
        sampleRate: Int = 16000,
        channels: Int = 1,
        encoding: String = "linear16"
    ): Boolean {
        if (isConnected || webSocket != null) {
            Log.w(TAG, "WebSocket connection already exists")
            return true
        }
        
        try {
            val url = buildWebSocketUrl(sampleRate, channels, encoding)
            Log.d(TAG, "Connecting to Deepgram WebSocket: $url")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token ${BuildConfig.DEEPGRAM_API_KEY}")
                .build()
            
            _connectionState.value = ConnectionState.CONNECTING
            
            webSocket = okHttpClient.newWebSocket(request, DeepgramWebSocketListener())
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WebSocket connection", e)
            _connectionState.value = ConnectionState.ERROR
            return false
        }
    }
    
    /**
     * Build the WebSocket URL with query parameters including keywords.
     */
    private fun buildWebSocketUrl(sampleRate: Int, channels: Int, encoding: String): String {
        val baseUrl = DEEPGRAM_WS_URL
        val params = mutableListOf<String>()
        
        // Basic audio parameters
        params.add("sample_rate=$sampleRate")
        params.add("channels=$channels")
        params.add("encoding=$encoding")
        
        // Language and model settings
        params.add("language=en")
        params.add("model=nova-2")
        
        // Real-time streaming settings
        params.add("interim_results=true")
        params.add("endpointing=300") // 300ms of silence to finalize utterance
        params.add("smart_format=true")
        params.add("punctuate=true")
        
        // Add keywords with intensifiers
        KEYWORDS.forEach { (keyword, intensifier) ->
            params.add("keywords=${keyword.replace(" ", "%20")}:$intensifier")
        }
        
        return "$baseUrl?${params.joinToString("&")}"
    }
    
    /**
     * Send audio data over the WebSocket connection.
     * 
     * @param audioData Raw audio bytes to send
     * @return true if data was sent successfully, false otherwise
     */
    fun sendAudioData(audioData: ByteArray): Boolean {
        return if (isConnected && webSocket != null) {
            try {
                webSocket?.send(ByteString.of(*audioData))
                updateLastAudioSentTime() // Track when we send audio for KeepAlive
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send audio data", e)
                false
            }
        } else {
            Log.w(TAG, "Cannot send audio data - WebSocket not connected")
            false
        }
    }
    
    /**
     * Close the WebSocket connection and clean up resources.
     */
    fun closeConnection() {
        try {
            stopKeepAlive() // Stop KeepAlive before closing
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            isConnected = false
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.d(TAG, "WebSocket connection closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing WebSocket connection", e)
        }
    }
    

    
    /**
     * Clear the accumulated transcript and reset transcription state.
     */
    fun clearTranscript() {
        transcriptBuilder.clear()
        interimTranscriptBuilder.clear()
        finalTranscriptBuilder.clear()
        lastInterimLength = 0
        _transcriptionResult.value = ""
        _isTranscribing.value = false
        _hasReceivedFirstResult.value = false
        Log.d(TAG, "Transcript cleared and transcription state reset")
    }
    
    /**
     * WebSocket listener for handling Deepgram streaming responses.
     */
    private inner class DeepgramWebSocketListener : WebSocketListener() {
        
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connection opened")
            isConnected = true
            _connectionState.value = ConnectionState.CONNECTED
            updateLastAudioSentTime() // Initialize the audio timestamp
            startKeepAlive() // Start the KeepAlive mechanism
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received WebSocket message: $text")
            
            try {
                val response = json.decodeFromString<DeepgramResponse>(text)
                
                // Only process transcription results, ignore other message types
                if (!response.isTranscriptionResult()) {
                    Log.d(TAG, "Received non-transcription message type: ${response.type}")
                    return
                }
                
                val transcript = response.getTranscript()
                
                // Skip empty transcripts
                if (transcript.isBlank()) {
                    Log.d(TAG, "Received empty transcript, skipping")
                    return
                }
                
                // Mark that we've received our first transcription result
                if (!_hasReceivedFirstResult.value) {
                    _hasReceivedFirstResult.value = true
                    _isTranscribing.value = true
                    Log.d(TAG, "First transcription result received, setting isTranscribing = true")
                }
                
                when {
                    response.isInterimResult() -> {
                        handleInterimResult(transcript)
                        Log.d(TAG, "Interim result: '$transcript'")
                    }
                    response.isFinalResult() -> {
                        handleFinalResult(transcript)
                        Log.d(TAG, "Final result: '$transcript'")
                    }
                    response.isSpeechFinalResult() -> {
                        handleSpeechFinalResult(transcript)
                        Log.d(TAG, "Speech final result: '$transcript'")
                    }
                    else -> {
                        Log.d(TAG, "Unknown result type, treating as interim: '$transcript'")
                        handleInterimResult(transcript)
                    }
                }
                
                // Update the current transcript state
                updateCurrentTranscript()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Deepgram response", e)
                Log.e(TAG, "Raw message: $text")
            }
        }
        
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Received binary WebSocket message: ${bytes.size} bytes")
            // Deepgram typically sends text messages, but handle binary if needed
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code - $reason")
            isConnected = false
            _connectionState.value = ConnectionState.DISCONNECTED
            stopKeepAlive() // Stop KeepAlive when closing
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code - $reason")
            isConnected = false
            _connectionState.value = ConnectionState.DISCONNECTED
            stopKeepAlive() // Stop KeepAlive when closed
            this@DeepgramRepository.webSocket = null
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            handleConnectionError(t, response) // Use centralized error handling
            isConnected = false
            stopKeepAlive() // Stop KeepAlive on failure
            this@DeepgramRepository.webSocket = null
        }
    }
    
    /**
     * Transcribe audio using real-time streaming.
     * This method provides a clean interface for the ViewModel, hiding the complexity
     * of WebSocket management and returning the final transcript as a String.
     * 
     * @param fallbackFile File to save audio chunks for fallback transcription
     * @return The final transcribed text as a String
     */
    suspend fun transcribeStream(fallbackFile: java.io.File): String {
        Log.d(TAG, "Starting streaming transcription")
        
        return try {
            // Clear any previous transcript
            clearTranscript()
            
            // Start audio streaming
            val streamingStarted = startAudioStreaming(fallbackFile)
            if (!streamingStarted) {
                throw Exception("Failed to start audio streaming")
            }
            
            // Wait for transcription to complete
            // This is a simplified implementation - in practice, this would be called
            // when the user stops recording and we need to wait for final results
            waitForFinalTranscription()
            
        } catch (e: Exception) {
            Log.e(TAG, "Streaming transcription failed", e)
            throw e
        }
    }
    
    /**
     * Wait for the final transcription results.
     * This method blocks until we have received final results or timeout.
     */
    private suspend fun waitForFinalTranscription(): String {
        // In a real implementation, this would wait for the user to stop recording
        // and then wait for any remaining final results from Deepgram
        
        // For now, return the current accumulated transcript
        return getCurrentTranscript()
    }
    
    /**
     * Get the current accumulated transcript (final + interim results).
     */
    fun getCurrentTranscript(): String {
        return finalTranscriptBuilder.toString()
    }
    
    /**
     * Get the current transcript including interim results for real-time display.
     */
    fun getCurrentTranscriptWithInterim(): String {
        return transcriptBuilder.toString()
    }
    
    /**
     * Placeholder method for batch transcription fallback.
     * This will be implemented in sub-task 4.3.
     * 
     * @param audioFile The audio file to transcribe
     * @return The transcribed text
     */
    suspend fun transcribeBatch(audioFile: java.io.File): String {
        Log.d(TAG, "transcribeBatch called - placeholder implementation")
        // TODO: Implement batch transcription fallback
        return "Batch transcription placeholder"
    }
    
    /**
     * Start streaming audio from the microphone to Deepgram.
     * This integrates StreamingAudioRecorder with the WebSocket connection.
     * 
     * @param fallbackFile File to save audio chunks for fallback transcription
     * @return true if streaming started successfully, false otherwise
     */
    fun startAudioStreaming(fallbackFile: java.io.File): Boolean {
        if (streamingJob != null) {
            Log.w(TAG, "Audio streaming already in progress")
            return true
        }
        
        try {
            // Initialize audio recorder
            audioRecorder = StreamingAudioRecorder()
            
            // Initialize WebSocket connection with audio recorder's configuration
            val audioConfig = audioRecorder!!.getAudioConfig()
            
            // Validate audio configuration
            if (!validateAudioConfiguration(audioConfig)) {
                throw Exception("Invalid audio configuration")
            }
            
            val connectionSuccess = initializeWebSocketConnection(
                sampleRate = audioConfig.sampleRate,
                channels = audioConfig.channels,
                encoding = audioConfig.encoding
            )
            
            if (!connectionSuccess) {
                Log.e(TAG, "Failed to initialize WebSocket connection")
                return false
            }
            
            // Start audio recording
            audioRecorder!!.startRecording(fallbackFile)
            
            // Start streaming job to send audio chunks over WebSocket
            streamingJob = CoroutineScope(Dispatchers.IO).launch {
                audioRecorder!!.audioChunks.collect { audioChunk ->
                    if (isConnected) {
                        val success = sendAudioData(audioChunk)
                        if (!success) {
                            Log.w(TAG, "Failed to send audio chunk, size: ${audioChunk.size}")
                        }
                    } else {
                        Log.w(TAG, "WebSocket not connected, skipping audio chunk")
                    }
                }
            }
            
            Log.d(TAG, "Audio streaming started successfully")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio streaming", e)
            stopAudioStreaming()
            return false
        }
    }
    
    /**
     * Stop audio streaming and return the fallback file.
     * 
     * @return The fallback audio file, or null if streaming wasn't active
     */
    fun stopAudioStreaming(): java.io.File? {
        var fallbackFile: java.io.File? = null
        
        try {
            // Stop streaming job
            streamingJob?.cancel()
            streamingJob = null
            
            // Stop audio recorder and get fallback file
            audioRecorder?.let { recorder ->
                fallbackFile = recorder.stopRecording()
            }
            audioRecorder = null
            
            // Close WebSocket connection
            closeConnection()
            
            Log.d(TAG, "Audio streaming stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio streaming", e)
        }
        
        return fallbackFile
    }
    
    /**
     * Check if audio streaming is currently active.
     */
    fun isStreaming(): Boolean {
        return streamingJob != null && audioRecorder != null
    }
    
    /**
     * Handle interim transcription results.
     * Interim results replace previous interim results for the same utterance.
     */
    private fun handleInterimResult(transcript: String) {
        // Replace the interim portion with the new interim result
        interimTranscriptBuilder.clear()
        interimTranscriptBuilder.append(transcript)
    }
    
    /**
     * Handle final transcription results.
     * Final results are committed to the final transcript.
     */
    private fun handleFinalResult(transcript: String) {
        // Commit the final result to the final transcript
        if (transcript.isNotBlank()) {
            if (finalTranscriptBuilder.isNotEmpty()) {
                finalTranscriptBuilder.append(" ")
            }
            finalTranscriptBuilder.append(transcript)
        }
        
        // Clear interim results since they're now finalized
        interimTranscriptBuilder.clear()
    }
    
    /**
     * Handle speech final results.
     * Speech final indicates the end of a natural speech segment.
     */
    private fun handleSpeechFinalResult(transcript: String) {
        // Treat speech final the same as final for transcript accumulation
        handleFinalResult(transcript)
        
        // Speech final indicates a natural break in speech
        Log.d(TAG, "Speech segment completed")
    }
    
    /**
     * Update the current transcript state by combining final and interim results.
     */
    private fun updateCurrentTranscript() {
        val combinedTranscript = buildString {
            // Add the final transcript
            append(finalTranscriptBuilder.toString())
            
            // Add interim results if any
            val interimText = interimTranscriptBuilder.toString()
            if (interimText.isNotBlank()) {
                if (isNotEmpty()) {
                    append(" ")
                }
                append(interimText)
            }
        }
        
        // Update the transcript state
        _transcriptionResult.value = combinedTranscript
        
        // Also update the legacy transcript builder for compatibility
        transcriptBuilder.clear()
        transcriptBuilder.append(combinedTranscript)
    }
    
    /**
     * Start the KeepAlive mechanism to prevent connection timeout.
     */
    private fun startKeepAlive() {
        stopKeepAlive() // Stop any existing keepalive
        
        keepAliveJob = CoroutineScope(Dispatchers.IO).launch {
            while (isConnected && webSocket != null) {
                try {
                    kotlinx.coroutines.delay(keepAliveIntervalMs)
                    
                    val timeSinceLastAudio = System.currentTimeMillis() - lastAudioSentTime
                    
                    // Send keepalive if we haven't sent audio recently
                    if (timeSinceLastAudio >= keepAliveIntervalMs) {
                        sendKeepAliveMessage()
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in KeepAlive loop", e)
                    break
                }
            }
        }
        
        Log.d(TAG, "KeepAlive mechanism started")
    }
    
    /**
     * Stop the KeepAlive mechanism.
     */
    private fun stopKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = null
        Log.d(TAG, "KeepAlive mechanism stopped")
    }
    
    /**
     * Send a KeepAlive message to maintain the WebSocket connection.
     * Uses a JSON control message as recommended by Deepgram.
     */
    private fun sendKeepAliveMessage() {
        try {
            val keepAliveMessage = """{"type": "KeepAlive"}"""
            webSocket?.send(keepAliveMessage)
            Log.d(TAG, "KeepAlive message sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send KeepAlive message", e)
        }
    }
    
    /**
     * Update the timestamp when audio data is sent.
     */
    private fun updateLastAudioSentTime() {
        lastAudioSentTime = System.currentTimeMillis()
    }
    
    /**
     * Handle common Deepgram disconnection scenarios and errors.
     */
    private fun handleConnectionError(error: Throwable, response: Response?) {
        val errorMessage = when {
            error.message?.contains("timeout", ignoreCase = true) == true -> {
                "Connection timeout - likely due to no audio data being sent for 10+ seconds"
            }
            error.message?.contains("encoding", ignoreCase = true) == true -> {
                "Audio encoding error - encoding parameter may not match actual audio format"
            }
            error.message?.contains("network", ignoreCase = true) == true ||
            error.message?.contains("connection", ignoreCase = true) == true -> {
                "Network connectivity issue - check internet connection"
            }
            response?.code == 401 -> {
                "Authentication failed - check Deepgram API key"
            }
            response?.code == 400 -> {
                "Bad request - invalid query parameters or audio format"
            }
            else -> {
                "WebSocket connection failed: ${error.message}"
            }
        }
        
        Log.e(TAG, "Connection error: $errorMessage", error)
        _connectionState.value = ConnectionState.ERROR
    }
    
    /**
     * Validate audio configuration before starting streaming.
     */
    private fun validateAudioConfiguration(audioConfig: StreamingAudioRecorder.AudioConfig): Boolean {
        return when {
            audioConfig.sampleRate !in listOf(8000, 16000, 22050, 44100, 48000) -> {
                Log.e(TAG, "Invalid sample rate: ${audioConfig.sampleRate}. Supported: 8000, 16000, 22050, 44100, 48000")
                false
            }
            audioConfig.channels !in 1..2 -> {
                Log.e(TAG, "Invalid channel count: ${audioConfig.channels}. Supported: 1 (mono) or 2 (stereo)")
                false
            }
            audioConfig.encoding !in listOf("linear16", "mulaw", "alaw") -> {
                Log.e(TAG, "Invalid encoding: ${audioConfig.encoding}. Supported: linear16, mulaw, alaw")
                false
            }
            else -> {
                Log.d(TAG, "Audio configuration validated: ${audioConfig.sampleRate}Hz, ${audioConfig.channels}ch, ${audioConfig.encoding}")
                true
            }
        }
    }
    
    /**
     * Check if we should attempt to reconnect based on the error type.
     */
    private fun shouldAttemptReconnection(error: Throwable): Boolean {
        return when {
            error.message?.contains("timeout", ignoreCase = true) == true -> false // Don't reconnect on timeout
            error.message?.contains("401", ignoreCase = true) == true -> false // Don't reconnect on auth failure
            error.message?.contains("400", ignoreCase = true) == true -> false // Don't reconnect on bad request
            error.message?.contains("network", ignoreCase = true) == true -> true // Reconnect on network issues
            error.message?.contains("connection", ignoreCase = true) == true -> true // Reconnect on connection issues
            else -> true // Default to attempting reconnection
        }
    }
} 