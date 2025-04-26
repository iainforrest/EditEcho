package com.example.editecho.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.editecho.network.AssistantApiClient
import com.example.editecho.network.WhisperRepository
import com.example.editecho.prompt.ToneProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/* ---------- UI state wrappers ---------- */
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

/* ---------- ViewModel ---------- */
class EditEchoOverlayViewModel(
    private val context: Context,
    private val whisperRepo: WhisperRepository = WhisperRepository(),
    private val assistant: AssistantApiClient = AssistantApiClient(),
) : ViewModel() {

    companion object {
        private const val TAG = "EditEchoViewModel"
    }

    /* Media-recorder stuff */
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    /* UI state */
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _toneState = MutableStateFlow<ToneState>(ToneState.Idle)
    val toneState: StateFlow<ToneState> = _toneState.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _refinedText = MutableStateFlow("")
    val refinedText: StateFlow<String> = _refinedText.asStateFlow()

    private val _selectedTone = MutableStateFlow(ToneProfile.FRIENDLY)
    val selectedTone: ToneProfile
        get() = _selectedTone.value

    /* ---------- Public helpers ---------- */
    fun setTone(tone: ToneProfile) {
        _selectedTone.value = tone
    }

    fun startRecording() = viewModelScope.launch {
        try {
            audioFile = File(context.cacheDir, "audio_record.m4a")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setAudioChannels(1)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            _recordingState.value = RecordingState.Recording
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start recording", e)
            _recordingState.value = RecordingState.Error("Start recording error: ${e.message}")
        }
    }

    fun stopRecording() = viewModelScope.launch {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _recordingState.value = RecordingState.Processing
            transcribeAndRefine()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to stop recording", e)
            _recordingState.value = RecordingState.Error("Stop recording error: ${e.message}")
        }
    }

    /* ---------- Core flow ---------- */
    private fun transcribeAndRefine() = viewModelScope.launch {
        try {
            val file = audioFile ?: error("No audio file")
            val transcript = withContext(Dispatchers.IO) { whisperRepo.transcribe(file) }

            _transcribedText.value = transcript
            _toneState.value = ToneState.Processing
            
            // Clear previous refined text
            _refinedText.value = ""
            
            // Use streaming API
            assistant.startRunStreaming(
                tone = selectedTone.fullLabel,
                rawText = transcript,
                onToken = { token ->
                    // Update UI with each token as it arrives
                    _refinedText.value += token
                },
                onDone = { finalText ->
                    // Update UI with final text
                    _refinedText.value = finalText
                    _toneState.value = ToneState.Success(finalText)
                    
                    // Auto-copy to clipboard on main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("EditEcho", finalText))
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                    
                    _recordingState.value = RecordingState.Idle
                },
                onError = { error ->
                    Log.e(TAG, "Streaming error", error)
                    _recordingState.value = RecordingState.Error("Processing error: ${error.message}")
                    _toneState.value = ToneState.Error("Processing error: ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Processing failed", e)
            _recordingState.value = RecordingState.Error("Processing error: ${e.message}")
            _toneState.value = ToneState.Error("Processing error: ${e.message}")
        }
    }

    /* ---------- Cleanup ---------- */
    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
        audioFile?.delete()
        audioFile = null
    }
}
