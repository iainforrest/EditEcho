package com.example.editecho.view

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.editecho.network.AssistantApiClient
import com.example.editecho.network.OpenAIProvider
import com.example.editecho.network.WhisperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import com.example.editecho.prompt.ToneProfile

sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Processing : RecordingState()
    data class Error(val message: String) : RecordingState()
}

sealed class ToneState {
    object Idle : ToneState()
    object Processing : ToneState()
    data class Success(val text: String) : ToneState()
    data class Error(val message: String) : ToneState()
}

class EditEchoOverlayViewModel(
    private val context: Context,
    private val whisperRepo: WhisperRepository = WhisperRepository(),
    private val assistant: AssistantApiClient = AssistantApiClient(OpenAIProvider.client)
) : ViewModel() {

    companion object {
        private const val TAG = "EditEchoViewModel"
    }

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _toneState = MutableStateFlow<ToneState>(ToneState.Idle)
    val toneState: StateFlow<ToneState> = _toneState.asStateFlow()
    
    // StateFlow for transcribed text
    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()
    
    // StateFlow for refined text
    private val _refinedText = MutableStateFlow("")
    val refinedText: StateFlow<String> = _refinedText.asStateFlow()

    private val _selectedTone = MutableStateFlow(ToneProfile.FRIENDLY)
    val selectedTone: ToneProfile
        get() = _selectedTone.value

    init {
        // Initialize any necessary components
    }

    fun setTone(tone: ToneProfile) {
        _selectedTone.value = tone
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting recording process...")
                audioFile = File(context.cacheDir, "audio_record.m4a")
                Log.d(TAG, "Audio file path: ${audioFile?.absolutePath}")

                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Log.d(TAG, "Using new MediaRecorder API")
                    MediaRecorder(context)
                } else {
                    Log.d(TAG, "Using legacy MediaRecorder API")
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    Log.d(TAG, "Configuring MediaRecorder...")
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000) // 128 kbps
                    setAudioSamplingRate(44100) // 44.1 kHz
                    setAudioChannels(1) // Mono audio
                    setOutputFile(audioFile?.absolutePath)
                    
                    Log.d(TAG, "Preparing MediaRecorder...")
                    prepare()
                    Log.d(TAG, "Starting MediaRecorder...")
                    start()
                }
                Log.d(TAG, "Recording started successfully")
                _recordingState.value = RecordingState.Recording
            } catch (e: IOException) {
                Log.e(TAG, "Error starting recording", e)
                _recordingState.value = RecordingState.Error("Failed to start recording: ${e.message}")
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Stopping recording...")
                mediaRecorder?.apply {
                    Log.d(TAG, "Stopping MediaRecorder...")
                    stop()
                    Log.d(TAG, "Releasing MediaRecorder...")
                    release()
                }
                mediaRecorder = null
                Log.d(TAG, "Recording stopped successfully")
                _recordingState.value = RecordingState.Processing
                processAudio()
            } catch (e: IOException) {
                Log.e(TAG, "Error stopping recording", e)
                _recordingState.value = RecordingState.Error("Failed to stop recording: ${e.message}")
            }
        }
    }

    private suspend fun processAudio() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting audio processing...")
                val file = audioFile ?: run {
                    Log.e(TAG, "No audio file found")
                    throw IOException("No audio file found")
                }
                Log.d(TAG, "Audio file exists: ${file.exists()}, size: ${file.length()} bytes")
                
                // Transcribe audio using Whisper API
                Log.d(TAG, "Starting Whisper API transcription...")
                val transcript = withContext(Dispatchers.IO) {
                    whisperRepo.transcribe(file)
                }
                Log.d(TAG, "Transcription completed successfully")
                
                // Update UI with raw transcription result
                _transcribedText.value = transcript
                _toneState.value = ToneState.Success(transcript)
                
                // Refine the transcript using the Assistant API
                try {
                    Log.d(TAG, "Starting Assistant API refinement with tone: ${selectedTone.fullLabel}")
                    _toneState.value = ToneState.Processing
                    
                    val refinedText = withContext(Dispatchers.IO) {
                        assistant.processTextWithTone(transcript, selectedTone.fullLabel)
                    }
                    Log.d(TAG, "Assistant API refinement completed successfully")
                    
                    // Update UI with refined text
                    _refinedText.value = refinedText
                    _toneState.value = ToneState.Success(refinedText)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during Assistant API refinement", e)
                    _toneState.value = ToneState.Error("Failed to refine text: ${e.message}")
                }
                
                _recordingState.value = RecordingState.Idle
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio", e)
                Log.e(TAG, "Error details: ${e.message}")
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                _recordingState.value = RecordingState.Error("Failed to process audio: ${e.message}")
                _toneState.value = ToneState.Error("Failed to process audio: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Cleaning up resources...")
        mediaRecorder?.release()
        mediaRecorder = null
        audioFile?.delete()
        audioFile = null
        Log.d(TAG, "Cleanup completed")
    }
} 