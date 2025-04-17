package com.example.editecho

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.editecho.ui.components.ToneButton
import com.example.editecho.ui.theme.EditEchoColors
import com.example.editecho.util.AudioRecorder
import com.example.editecho.util.OpenAIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.rememberCoroutineScope
import android.content.res.Configuration

/**
 * Composable function that displays the EditEcho overlay as a bottom sheet.
 *
 * @param onDismiss Callback function to be called when the user dismisses the overlay.
 */
@Composable
fun EditEchoOverlay(onDismiss: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for microphone permission
    var hasMicPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) 
    }
    
    // State for showing permission request dialog
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // State for error message
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // State for recording
    var isRecording by remember { mutableStateOf(false) }
    var isMicActive by remember { mutableStateOf(false) }
    
    // State for the text
    var text by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    // State for the tone selector
    var selectedTone by remember { mutableStateOf("Professional") }
    
    // MediaRecorder and MediaPlayer instances
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // State for transcribed text
    var transcribedText by remember { mutableStateOf("This is a placeholder for transcribed text. Tap the mic button to start recording.") }

    // OpenAI API Key - Loaded from BuildConfig
    val openAiApiKey = BuildConfig.OPENAI_API_KEY

    // State to track which tone is active
    var activeTone by remember { mutableStateOf("SMS") }

    // Create AudioRecorder instance
    val audioRecorder = remember { AudioRecorder(context) }

    // Function to check microphone permission
    fun checkMicPermission() {
        hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request microphone permission
    fun requestMicPermission() {
        // This would typically launch a permission request
        // For now, we'll just show a dialog
        showPermissionDialog = true
    }

    // Function to start recording
    fun startRecording() {
        if (!hasMicPermission) {
            requestMicPermission()
            return
        }

        try {
            // Create a temporary file to store the recording
            val audioFile = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d("EditEcho", "Recording started")
                } catch (e: Exception) {
                    Log.e("EditEcho", "Error starting recording: ${e.message}")
                    errorMessage = "Error starting recording: ${e.message}"
                }
            }
        } catch (e: Exception) {
            Log.e("EditEcho", "Error setting up MediaRecorder: ${e.message}")
            errorMessage = "Error setting up MediaRecorder: ${e.message}"
        }
    }

    // Function to stop recording
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.d("EditEcho", "Recording stopped")
            
            // Process the recorded audio using local function
            fun processAudioRecording() {
                // This would typically send the audio file to a speech-to-text service
                // For now, we'll just simulate a delay and set some placeholder text
                scope.launch {
                    isProcessing = true
                    delay(2000) // Simulate processing time
                    transcribedText = "This is a simulated transcription of your audio recording. In a real implementation, this would be the result from a speech-to-text service."
                    isProcessing = false
                }
            }
            
            processAudioRecording()
        } catch (e: Exception) {
            Log.e("EditEcho", "Error stopping recording: ${e.message}")
            errorMessage = "Error stopping recording: ${e.message}"
        }
    }

    // Function to send text to OpenAI API
    fun sendTextToOpenAI(text: String, tone: String) {
        scope.launch {
            try {
                isProcessing = true
                
                // Create a client for making HTTP requests
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                // Create the request body
                val jsonBody = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONObject().apply {
                        put("role", "system")
                        put("content", "You are a helpful assistant that rewrites text in a $tone tone.")
                    })
                    put("messages", JSONObject().apply {
                        put("role", "user")
                        put("content", text)
                    })
                }.toString()
                
                // Create the request
                val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $openAiApiKey")
                    .post(requestBody)
                    .build()
                
                // Make the request
                withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val jsonResponse = JSONObject(responseBody)
                        val choices = jsonResponse.getJSONArray("choices")
                        val firstChoice = choices.getJSONObject(0)
                        val message = firstChoice.getJSONObject("message")
                        val content = message.getString("content")
                        
                        // Update the text with the response
                        withContext(Dispatchers.Main) {
                            transcribedText = content
                            isProcessing = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Error: ${response.code} - ${response.message.orEmpty()}"
                            isProcessing = false
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${e.message}"
                    isProcessing = false
                }
            }
        }
    }

    // Show error message if there is one
    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red.copy(alpha = 0.8f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage ?: "An error occurred",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    
    // Show permission request dialog if needed
    if (showPermissionDialog) {
        Dialog(
            onDismissRequest = { showPermissionDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Microphone Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "EditEcho needs microphone permission to record audio. Please grant the permission in your device settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showPermissionDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                showPermissionDialog = false
                                requestMicPermission()
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditEchoColors.Background)
    ) {
        // Dimmed background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )

        // Bottom Sheet Content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // 70% of screen height
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = EditEchoColors.Surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EditEcho",
                        style = MaterialTheme.typography.titleLarge,
                        color = EditEchoColors.Primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = EditEchoColors.Primary
                        )
                    }
                }

                // TextField for transcribed text
                TextField(
                    value = transcribedText,
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = false,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = EditEchoColors.Surface,
                        focusedContainerColor = EditEchoColors.Surface,
                        unfocusedTextColor = EditEchoColors.PrimaryText,
                        focusedTextColor = EditEchoColors.PrimaryText
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = EditEchoColors.PrimaryText
                    )
                )

                // Tone Selection Buttons
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select Tone:",
                    style = MaterialTheme.typography.titleSmall,
                    color = EditEchoColors.PrimaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToneButton(
                        text = "SMS",
                        isActive = activeTone == "SMS",
                        onClick = { activeTone = "SMS" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ToneButton(
                        text = "Email",
                        isActive = activeTone == "Email",
                        onClick = { activeTone = "Email" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ToneButton(
                        text = "Professional",
                        isActive = activeTone == "Professional",
                        onClick = { activeTone = "Professional" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action Buttons
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Mic Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMicActive) EditEchoColors.Primary else Color.Gray)
                            .clickable {
                                // Toggle mic state
                                isMicActive = !isMicActive

                                if (isMicActive) {
                                    // Start recording
                                    startRecording()
                                    // Clear text when starting recording
                                    transcribedText = ""
                                } else {
                                    // Stop recording and send to Whisper API
                                    stopRecording()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMicActive) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (isMicActive) "Stop Recording" else "Start Recording",
                            tint = Color.White
                        )
                    }

                    // Copy Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(EditEchoColors.Secondary)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Transcribed Text", transcribedText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White
                        )
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(EditEchoColors.Accent)
                            .clickable { /* TODO: Implement settings */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditEchoOverlayPreview() {
    EditEchoTheme {
        EditEchoOverlay()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditEchoOverlayDarkPreview() {
    EditEchoTheme(darkTheme = true) {
        EditEchoOverlay()
    }
} 