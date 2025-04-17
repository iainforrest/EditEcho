package com.example.editecho

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.editecho.network.AssistantApiClient
import androidx.compose.runtime.rememberCoroutineScope
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import android.content.res.Configuration
import com.example.editecho.ui.theme.EditEchoTheme


// Function to send audio file to OpenAI Whisper API
fun sendAudioToWhisperApi(audioFile: File, apiKey: String): String {
    Log.d("WhisperAPI", "Starting API call with file: ${audioFile.absolutePath}")
    Log.d("WhisperAPI", "File exists: ${audioFile.exists()}, File size: ${audioFile.length()} bytes")

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Create multipart request body
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            audioFile.name,
            audioFile.asRequestBody("audio/m4a".toMediaType())
        )
        .addFormDataPart("model", "whisper-1")
        .build()

    Log.d("WhisperAPI", "Created request body")

    // Create request
    val request = Request.Builder()
        .url("https://api.openai.com/v1/audio/transcriptions")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestBody)
        .build()

    Log.d("WhisperAPI", "Sending request to OpenAI API")

    // Execute request
    client.newCall(request).execute().use { response ->
        Log.d("WhisperAPI", "Received response with code: ${response.code}")

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            Log.e("WhisperAPI", "API call failed: ${response.code} - $errorBody")
            throw IOException("API call failed: ${response.code}")
        }

        // Parse response
        val responseBody = response.body?.string() ?: throw IOException("Empty response body")
        Log.d("WhisperAPI", "Received response body: $responseBody")

        val jsonResponse = JSONObject(responseBody)
        val text = jsonResponse.optString("text", "No transcription available")
        Log.d("WhisperAPI", "Extracted text: $text")
        return text
    }
}

/**
 * Composable function that displays a floating bubble overlay.
 *
 * @param onClose Callback function to be called when the user closes the overlay.
 */
@Composable
fun FloatingBubbleOverlay(onClose: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
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
    
    // State for the floating bubble
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
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

    // Create AssistantApiClient instance
    val assistantApiClient = remember { AssistantApiClient(context) }

    // Function to check microphone permission
    fun checkMicPermission() {
        hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasMicPermission) {
            showPermissionDialog = true
        }
    }
    
    // Function to request microphone permission
    fun requestMicPermission() {
        if (context is Activity) {
            // Instead of using registerForActivityResult, we'll use a simpler approach
            // by showing a dialog with instructions to go to settings
            showPermissionDialog = true
        } else {
            // If not in an Activity, show a dialog with instructions
            showPermissionDialog = true
        }
    }

    // Function to create a MediaRecorder with modern API
    fun createMediaRecorder(outputFile: File): MediaRecorder {
        return MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
        }
    }

    // Function to start recording
    fun startRecording() {
        try {
            // Check for microphone permission first
            if (!hasMicPermission) {
                checkMicPermission()
                return
            }
            
            // Create a temporary file for the recording
            val outputFile = File(context.cacheDir, "audio_recording_${System.currentTimeMillis()}.m4a")
            
            // Create and start the MediaRecorder
            mediaRecorder = createMediaRecorder(outputFile)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            
            isRecording = true
            isMicActive = true
        } catch (e: Exception) {
            Log.e("FloatingBubble", "Error starting recording: ${e.message}")
            e.printStackTrace()
            errorMessage = "Error starting recording: ${e.message}"
        }
    }

    // Function to stop recording
    fun stopRecording() {
        try {
            // Stop and release MediaRecorder
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            isRecording = false
            isMicActive = false
        } catch (e: Exception) {
            Log.e("FloatingBubble", "Error stopping recording: ${e.message}")
            e.printStackTrace()
            errorMessage = "Error stopping recording: ${e.message}"
        }
    }

    // Use BackHandler for predictive back handling (API 31+)
    BackHandler(enabled = isExpanded) {
        isExpanded = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditEchoColors.Background)
    ) {
        // Dimmed background when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isExpanded = false }
            )
        }

        // Floating Bubble
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, EditEchoColors.Primary, CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isExpanded = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EE",
                    color = EditEchoColors.Primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        // Bottom Sheet and Floating Action Buttons
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it }, // Start from below the screen
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, // Exit to below the screen
                animationSpec = tween(300)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.25f) // 25% of screen height
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bottom Sheet with Text Area (80% width)
                Surface(
                    modifier = Modifier
                        .weight(0.8f) // 80% of available width
                        .fillMaxHeight()
                        .clickable(enabled = false) { }, // Prevent clicks from propagating to the dimmed background
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = EditEchoColors.Surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // TextField
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
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 0.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ToneButton(
                                text = "SMS",
                                isActive = activeTone == "SMS",
                                onClick = { activeTone = "SMS" },
                                modifier = Modifier.weight(1f)
                            )
                            ToneButton(
                                text = "Email",
                                isActive = activeTone == "Email",
                                onClick = { activeTone = "Email" },
                                modifier = Modifier.weight(1f)
                            )
                            ToneButton(
                                text = "Pro",
                                isActive = activeTone == "Pro",
                                onClick = { activeTone = "Pro" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Floating Action Buttons (20% width, outside the bottom sheet)
                Column(
                    modifier = Modifier
                        .weight(0.2f) // 20% of available width
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Close and Settings buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Settings button
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, EditEchoColors.Primary, CircleShape)
                                .clickable { /* TODO: Implement settings */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = EditEchoColors.Primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, EditEchoColors.Primary, CircleShape)
                                .clickable {
                                    // Close the app
                                    onClose()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = EditEchoColors.Primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // EE Logo Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, EditEchoColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EE",
                            color = EditEchoColors.Primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Mic Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EditEchoColors.Secondary)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Transcribed Text", transcribedText)
                                clipboard.setPrimaryClip(clip)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White
                        )
                    }
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
}

@Composable
fun FeatureButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(EditEchoColors.Primary.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = EditEchoColors.Primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = EditEchoColors.PrimaryText
        )
    }
}

@Composable
fun ToneButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp), // adjust height here as needed (smaller = tighter)
        color = if (isActive) EditEchoColors.Primary else EditEchoColors.Surface,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, EditEchoColors.Primary),
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (isActive) Color.White else EditEchoColors.Primary,
                modifier = Modifier.padding(vertical = 0.dp)
            )
        }
    }
}

// Definition for LocalOnBackPressedDispatcherOwner if it doesn't exist elsewhere
val LocalOnBackPressedDispatcherOwner =
    staticCompositionLocalOf<OnBackPressedDispatcherOwner> {
        error("No OnBackPressedDispatcherOwner provided")
    }

@Preview(showBackground = true)
@Composable
fun FloatingBubbleOverlayPreview() {
    EditEchoTheme {
        FloatingBubbleOverlay()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FloatingBubbleOverlayDarkPreview() {
    EditEchoTheme(darkTheme = true) {
        FloatingBubbleOverlay()
    }
}