package com.editecho.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.editecho.ui.theme.EditEchoColors
import com.editecho.view.EditEchoOverlayViewModel
import com.editecho.view.RecordingState
import com.editecho.view.ToneState
import android.widget.Toast
import android.util.Log
import com.editecho.ui.components.EditedMessageBox
import com.editecho.prompt.VoiceSettings

/**
 * Reusable content for the EditEcho overlay that can be used in both Dialog and WindowManager contexts.
 * This contains the main UI elements: header, text display, sliders, and action buttons.
 */
@Composable
fun EditEchoOverlayContent(
    recordingState: RecordingState,
    toneState: ToneState,
    voiceSettings: VoiceSettings,
    refinedText: String,
    onDismiss: () -> Unit,
    onFormalityChanged: (Int) -> Unit,
    onPolishChanged: (Int) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCopyToClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = recordingState is RecordingState.Recording
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main content area with two columns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left column - Text and tone selector
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
            ) {
                EditedMessageBox(
                    recordingState = recordingState,
                    toneState = toneState,
                    editedText = refinedText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Sliders Section
                Column(modifier = Modifier.weight(0.3f).padding(bottom = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Formality", 
                            style = MaterialTheme.typography.bodySmall,
                            color = EditEchoColors.PrimaryText,
                            modifier = Modifier.width(65.dp)
                        )
                        Slider(
                            value = voiceSettings.formality.toFloat(),
                            onValueChange = { onFormalityChanged(it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 100,
                            modifier = Modifier.weight(1f).heightIn(min = 30.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = EditEchoColors.Accent,
                                activeTrackColor = EditEchoColors.Accent,
                                inactiveTrackColor = EditEchoColors.Accent.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Polish Slider Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Polish", 
                            style = MaterialTheme.typography.bodySmall,
                            color = EditEchoColors.PrimaryText,
                            modifier = Modifier.width(65.dp)
                        )
                        Slider(
                            value = voiceSettings.polish.toFloat(),
                            onValueChange = { onPolishChanged(it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 100,
                            modifier = Modifier.weight(1f).heightIn(min = 30.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = EditEchoColors.Accent,
                                activeTrackColor = EditEchoColors.Accent,
                                inactiveTrackColor = EditEchoColors.Accent.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
            
            // Right column - Vertical buttons, including X button at the top
            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .fillMaxHeight()
                    .padding(start = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = EditEchoColors.PrimaryText
                    )
                }
                
                IconButton(
                    onClick = { /* Settings functionality removed */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = EditEchoColors.Primary
                    )
                }
                
                IconButton(
                    onClick = onCopyToClipboard,
                    enabled = refinedText.isNotEmpty(),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Text",
                        tint = if (refinedText.isNotEmpty()) EditEchoColors.Primary else EditEchoColors.Primary.copy(alpha = 0.5f)
                    )
                }
                
                IconButton(
                    onClick = { if (isRecording) onStopRecording() else onStartRecording() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = if (isRecording) EditEchoColors.Error else EditEchoColors.Primary
                    )
                }
            }
        }
    }
}

/**
 * A bottom sheet overlay that provides audio recording, transcription, and tone adjustment functionality.
 *
 * @param onDismiss Callback to be invoked when the overlay is dismissed.
 */
@Composable
fun EditEchoOverlay(
    onDismiss: () -> Unit,
    viewModel: EditEchoOverlayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsState()
    val toneState by viewModel.toneState.collectAsState()
    val voiceSettings by viewModel.voiceSettings.collectAsState()
    val refinedText by viewModel.refinedText.collectAsState()
    
    // State variables
    var hasMicPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Remove redundant state variables that are now in ViewModel
    val isRecording = recordingState is RecordingState.Recording
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (!isGranted) {
            errorMessage = "Microphone permission is required for recording"
        }
    }
    
    // Check for microphone permission
    LaunchedEffect(Unit) {
        hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Request microphone permission if not granted
    fun requestMicPermission() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    // Start recording
    fun startRecording() {
        if (!hasMicPermission) {
            requestMicPermission()
            return
        }
        viewModel.startRecording()
    }
    
    // Stop recording and process audio
    fun stopRecording() {
        viewModel.stopRecording()
    }
    
    // Copy refined text to clipboard
    fun copyTextToClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("EditEcho Text", refinedText)
            clipboard.setPrimaryClip(clip)
            Log.d("EditEchoOverlay", "Successfully copied to clipboard")
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("EditEchoOverlay", "Failed to copy to clipboard", e)
            Toast.makeText(context, "Failed to copy text", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Bottom sheet dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable(enabled = false) { },
                colors = CardDefaults.cardColors(
                    containerColor = EditEchoColors.Surface
                )
            ) {
                EditEchoOverlayContent(
                    recordingState = recordingState,
                    toneState = toneState,
                    voiceSettings = voiceSettings,
                    refinedText = refinedText,
                    onDismiss = onDismiss,
                    onFormalityChanged = viewModel::onFormalityChanged,
                    onPolishChanged = viewModel::onPolishChanged,
                    onStartRecording = { startRecording() },
                    onStopRecording = { stopRecording() },
                    onCopyToClipboard = { copyTextToClipboard() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Error message dialog
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
} 