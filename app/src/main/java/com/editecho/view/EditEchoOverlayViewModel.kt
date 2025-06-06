package com.editecho.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editecho.data.SettingsRepository
import com.editecho.network.AssistantApiClient
import com.editecho.network.ChatCompletionClient
import com.editecho.network.ClaudeCompletionClient
import com.editecho.network.WhisperRepository
// import com.editecho.prompt.ToneProfile
import com.editecho.prompt.VoiceSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

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
@HiltViewModel
class EditEchoOverlayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whisperRepo: WhisperRepository,
    private val assistant: AssistantApiClient,
    private val chatCompletionClient: ChatCompletionClient,
    private val claudeCompletionClient: ClaudeCompletionClient,
    private val settings: SettingsRepository
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

    // Updated voice settings state
    private val _voiceSettings = MutableStateFlow(VoiceSettings(formality = 50, polish = 50))
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    init {
        // Load saved voice settings from SettingsRepository
        viewModelScope.launch {
            combine(
                settings.formality,
                settings.polish
            ) { formality, polish ->
                VoiceSettings(formality = formality, polish = polish)
            }.collect { savedSettings ->
                Log.d(TAG, "Loaded saved voice settings: Formality ${savedSettings.formality}, Polish ${savedSettings.polish}")
                _voiceSettings.value = savedSettings
            }
        }
    }

    /* ---------- Public helpers ---------- */
    fun formatExample(): String = _refinedText.value

    private fun appendToHistory(prefix: String, text: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "history.txt")
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            // Format as CSV: timestamp,type,text
            // Replace any commas in the text with semicolons to avoid breaking CSV format
            val sanitizedText = text.replace(",", ";")
            val entry = "$timestamp,$prefix,$sanitizedText\n"
            file.appendText(entry)
            Log.d(TAG, "Successfully appended to history.txt")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to append to history.txt", e)
        }
    }

    fun saveExample() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "history.txt")
            val formattedText = formatExample()
            Log.d(TAG, "Saving to history.txt at ${file.absolutePath}")
            file.appendText(formattedText + "\n\n")
            Log.d(TAG, "Successfully saved to history.txt")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Example saved", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save to history.txt", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save example", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // New voice settings update functions
    fun onFormalityChanged(value: Int) {
        Log.d(TAG, "Formality changed to: $value")
        _voiceSettings.value = _voiceSettings.value.copy(formality = value)
        // Save to settings repository
        viewModelScope.launch {
            settings.setFormality(value)
            Log.d(TAG, "Saved formality setting: $value")
        }
    }
    
    fun onPolishChanged(value: Int) {
        Log.d(TAG, "Polish changed to: $value")
        _voiceSettings.value = _voiceSettings.value.copy(polish = value)
        // Save to settings repository
        viewModelScope.launch {
            settings.setPolish(value)
            Log.d(TAG, "Saved polish setting: $value")
        }
    }

    // Commented out old tone selection function
    // fun onToneSelected(tone: ToneProfile) {
    //     _selectedTone.value = tone
    // }

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
            
            // Set transcribed text for UI display
            _transcribedText.value = transcript
            
            // Log the transcription to history
            appendToHistory("Transcription", transcript)
            
            // Set both states to Processing to show "Editing" state
            _recordingState.value = RecordingState.Idle
            _toneState.value = ToneState.Processing
            
            // Clear previous refined text
            _refinedText.value = ""
            
            /* 
            // COMMENTED OUT: Old streaming GPT implementation
            // Commented out TRANSCRIBE_ONLY check for now
            // if (selectedTone.value == ToneProfile.TRANSCRIBE_ONLY) {
            //     _refinedText.value = transcript
            //     _toneState.value = ToneState.Success(transcript)
            //     appendToHistory("Edited", transcript)
            //     return@launch
            // }
            
            // Use chat completions API directly for other tones
            var finalText = ""
            Log.d(TAG, "Starting to collect tokens from OpenAI")
            
            // Temporarily comment out chat completion call until new system is ready
            // Create a StringBuilder to accumulate the text
            // val textBuilder = StringBuilder()
            
            // Collect all tokens
            // chatCompletionClient.streamReply(selectedTone.value, transcript).collect { token ->
            //     _refinedText.value += token
            //     textBuilder.append(token)
            //     Log.d(TAG, "Received token: '$token', current length: ${textBuilder.length}")
            // }
            
            // For now, just use the transcript as final text
            finalText = transcript
            _refinedText.value = finalText
            
            // Get the final text after the stream is complete
            // finalText = textBuilder.toString()
            Log.d(TAG, "Stream complete, final text: '$finalText'")
            */
            
            // NEW: Use Claude API with Voice DNA
            Log.d(TAG, "Using Claude API with Voice DNA - Formality: ${voiceSettings.value.formality}, Polish: ${voiceSettings.value.polish}")
            
            val edited = try {
                claudeCompletionClient.complete(voiceSettings.value, transcript)
            } catch (e: Exception) {
                Log.e(TAG, "Claude API failed, falling back to transcript", e)
                // Fall back to original transcript if Claude fails
                transcript
            }
            
            // Update refined text
            _refinedText.value = edited
            
            Log.d(TAG, "Claude editing complete. Original: '$transcript' -> Edited: '$edited'")
            
            // Log the edited text to history with voice settings
            appendToHistory("Edited,F${voiceSettings.value.formality}P${voiceSettings.value.polish}", edited)
            
            // Update UI with final text
            _toneState.value = ToneState.Success(edited)
            
            // Auto-copy to clipboard on main thread
            Log.d(TAG, "Attempting to copy to clipboard: '$edited'")
            viewModelScope.launch(Dispatchers.Main) {
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("EditEcho", edited))
                    Log.d(TAG, "Successfully copied to clipboard")
                    Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy to clipboard", e)
                }
            }
            
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
