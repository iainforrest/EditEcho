package com.editecho.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editecho.data.SettingsRepository
import com.editecho.data.VoiceDNARepository
import com.editecho.network.AssistantApiClient
import com.editecho.network.ChatCompletionClient
import com.editecho.network.ClaudeCompletionClient
import com.editecho.network.DeepgramRepository
import com.editecho.prompt.ToneProfile

import com.editecho.prompt.VoicePromptBuilder
import com.editecho.util.FormalityMapper
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
    private val deepgramRepo: DeepgramRepository,
    private val assistant: AssistantApiClient,
    private val chatCompletionClient: ChatCompletionClient,
    private val claudeCompletionClient: ClaudeCompletionClient,
    private val settings: SettingsRepository,
    private val voiceDNARepository: VoiceDNARepository
) : ViewModel() {

    companion object {
        private const val TAG = "EditEchoViewModel"
    }

    /* Deepgram streaming audio recording */
    private var fallbackAudioFile: File? = null

    /* UI state */
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _toneState = MutableStateFlow<ToneState>(ToneState.Idle)
    val toneState: StateFlow<ToneState> = _toneState.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _refinedText = MutableStateFlow("")
    val refinedText: StateFlow<String> = _refinedText.asStateFlow()

    // New state for tracking when transcription starts (sub-task 5.2)
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()
    
    // Voice Engine 3.0 state - tone selection and polish level
    private val _selectedTone = MutableStateFlow(ToneProfile.getDefault())
    val selectedTone: StateFlow<ToneProfile> = _selectedTone.asStateFlow()
    
    private val _polishLevel = MutableStateFlow(50)
    val polishLevel: StateFlow<Int> = _polishLevel.asStateFlow()

    init {
        // Load saved Voice Engine 3.0 settings from SettingsRepository
        viewModelScope.launch {
            // Load selectedTone and convert from String to ToneProfile
            settings.selectedTone.collect { toneString ->
                val toneProfile = ToneProfile.fromName(toneString) ?: ToneProfile.getDefault()
                Log.d(TAG, "Loaded saved tone: $toneString -> ${toneProfile.displayName}")
                _selectedTone.value = toneProfile
            }
        }
        
        viewModelScope.launch {
            // Load polishLevel
            settings.polishLevel.collect { level ->
                Log.d(TAG, "Loaded saved polish level: $level")
                _polishLevel.value = level
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



    // Voice Engine 3.0 functions
    fun onToneSelected(tone: ToneProfile) {
        Log.d(TAG, "Tone selected: ${tone.displayName}")
        _selectedTone.value = tone
        // Save to settings repository immediately
        viewModelScope.launch {
            settings.setSelectedTone(tone.displayName)
            Log.d(TAG, "Saved tone setting: ${tone.displayName}")
        }
        // When tone changes, update formality calculation automatically
        updateFormalityFromToneAndPolish()
    }
    
    fun onPolishLevelChanged(polishLevel: Int) {
        Log.d(TAG, "Polish level changed to: $polishLevel")
        _polishLevel.value = polishLevel
        // Save to settings repository immediately
        viewModelScope.launch {
            settings.setPolishLevel(polishLevel)
            Log.d(TAG, "Saved polish level setting: $polishLevel")
        }
        // Update formality calculation based on new polish level
        updateFormalityFromToneAndPolish()
    }
    
    private fun updateFormalityFromToneAndPolish() {
        val currentTone = _selectedTone.value
        val currentPolish = _polishLevel.value
        val calculatedFormality = FormalityMapper.calculateFormality(currentTone, currentPolish)
        
        Log.d(TAG, "Calculated formality: $calculatedFormality% for tone ${currentTone.displayName} at ${currentPolish}% polish")
        
        // TODO: Save settings using Voice Engine 3.0 approach
        // Will be implemented in next task
    }

    fun startRecording() = viewModelScope.launch {
        try {
            // Create fallback file for Deepgram streaming
            fallbackAudioFile = File(context.cacheDir, "deepgram_fallback_${System.currentTimeMillis()}.pcm")
            
            // Clear previous transcription state
            _transcribedText.value = ""
            _refinedText.value = ""
            _isTranscribing.value = false
            
            // Start Deepgram streaming
            val streamingStarted = deepgramRepo.startAudioStreaming(fallbackAudioFile!!)
            
            if (streamingStarted) {
                _recordingState.value = RecordingState.Recording
                Log.d(TAG, "Deepgram streaming started successfully")
                
                // Observe transcription state from Deepgram
                observeDeepgramTranscription()
            } else {
                throw Exception("Failed to start Deepgram audio streaming")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            _recordingState.value = RecordingState.Error("Start recording error: ${e.message}")
        }
    }

    fun stopRecording() = viewModelScope.launch {
        try {
            // Stop Deepgram streaming - this sends CloseStream message and waits for final results
            val finalFallbackFile = deepgramRepo.stopAudioStreaming()
            
            // Update UI state - Recording stops, but Transcribing continues
            _recordingState.value = RecordingState.Processing
            
            Log.d(TAG, "Recording stopped, waiting for final transcription results...")
            
            // Get the final transcript from Deepgram (this will wait for final results)
            transcribeAndRefine(finalFallbackFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            _recordingState.value = RecordingState.Error("Stop recording error: ${e.message}")
            _isTranscribing.value = false // Reset transcribing state on error
        }
    }
    
    /**
     * Observe Deepgram transcription state to update isTranscribing flag.
     */
    private fun observeDeepgramTranscription() {
        viewModelScope.launch {
            deepgramRepo.hasReceivedFirstResult.collect { hasFirstResult ->
                if (hasFirstResult && !_isTranscribing.value) {
                    _isTranscribing.value = true
                    Log.d(TAG, "First transcription result received, setting isTranscribing = true")
                }
            }
        }
    }

    /* ---------- Core flow ---------- */
    private fun transcribeAndRefine(fallbackFile: File?) = viewModelScope.launch {
        try {
            val file = fallbackFile ?: error("No fallback audio file")
            
            Log.d(TAG, "Getting final transcript from Deepgram...")
            val transcript = withContext(Dispatchers.IO) { deepgramRepo.transcribeStream(file) }
            
            Log.d(TAG, "Final transcript received: '$transcript'")
            
            // Set transcribed text for UI display
            _transcribedText.value = transcript
            
            // Log the transcription to history
            appendToHistory("Transcription", transcript)
            
            // Transcription is complete, now start editing
            _isTranscribing.value = false // Transcribing is done
            _recordingState.value = RecordingState.Idle
            _toneState.value = ToneState.Processing
            
            Log.d(TAG, "Starting Voice Engine 3.0 editing...")
            
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
            
            // Voice Engine 3.0: Use tone + polish with DNA patterns
            Log.d(TAG, "Using Voice Engine 3.0 - Tone: ${selectedTone.value.displayName}, Polish: ${polishLevel.value}%")
            
            val edited = try {
                // Build Voice Engine 3.0 prompt with DNA patterns
                val prompt = VoicePromptBuilder.buildPrompt(
                    tone = selectedTone.value,
                    polishLevel = polishLevel.value,
                    rawText = transcript,
                    repository = voiceDNARepository
                )
                
                Log.d(TAG, "Generated Voice Engine 3.0 prompt for ${selectedTone.value.displayName} tone")
                
                // Use Claude with the new prompt
                claudeCompletionClient.completeWithPrompt(prompt)
            } catch (e: Exception) {
                Log.e(TAG, "Voice Engine 3.0 failed, using transcript as fallback", e)
                transcript
            }
            
            // Update refined text
            _refinedText.value = edited
            
            Log.d(TAG, "Claude editing complete. Original: '$transcript' -> Edited: '$edited'")
            
            // Log the edited text to history with Voice Engine 3.0 settings
            val calculatedFormality = FormalityMapper.calculateFormality(selectedTone.value, polishLevel.value)
            appendToHistory("VE3,${selectedTone.value.displayName},P${polishLevel.value}F${calculatedFormality}", edited)
            
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
        // Clean up Deepgram resources
        deepgramRepo.closeConnection()
        fallbackAudioFile?.delete()
        fallbackAudioFile = null
    }
}
