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

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _toneState = MutableStateFlow<ToneState>(ToneState.Idle)
    val toneState: StateFlow<ToneState> = _toneState.asStateFlow()

    var selectedTone by mutableStateOf("Professional")
        private set

    fun setTone(tone: String) {
        selectedTone = tone
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                audioFile = File(context.cacheDir, "audio_record.mp3")
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile?.absolutePath)
                    prepare()
                    start()
                }
                _recordingState.value = RecordingState.Recording
            } catch (e: IOException) {
                Log.e("EditEchoOverlayViewModel", "Error starting recording", e)
                _recordingState.value = RecordingState.Error("Failed to start recording: ${e.message}")
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                _recordingState.value = RecordingState.Processing
                processAudio()
            } catch (e: IOException) {
                Log.e("EditEchoOverlayViewModel", "Error stopping recording", e)
                _recordingState.value = RecordingState.Error("Failed to stop recording: ${e.message}")
            }
        }
    }

    private suspend fun processAudio() {
        viewModelScope.launch {
            try {
                Log.d("EditEcho", "Starting processAudio...")
                val file = audioFile ?: run {
                    Log.e("EditEcho", "No audio file found.")
                    throw IOException("No audio file found")
                }
                Log.d("EditEcho", "Audio file path: ${file.absolutePath}")
                
                // Transcribe audio using Whisper API
                val rawText = withContext(Dispatchers.IO) {
                    whisperRepo.transcribe(file)
                }
                Log.d("EditEcho", "Transcription result: $rawText")

                // Process transcription with Assistants API
                _toneState.value = ToneState.Processing
                val refinedText = assistant.processTextWithTone(
                    rawText,
                    selectedTone
                )
                _toneState.value = ToneState.Success(refinedText)
                _recordingState.value = RecordingState.Idle
            } catch (e: Exception) {
                Log.e("EditEchoOverlayViewModel", "Error processing audio", e)
                _recordingState.value = RecordingState.Error("Failed to process audio: ${e.message}")
                _toneState.value = ToneState.Error("Failed to process audio: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
        audioFile?.delete()
        audioFile = null
    }
} 