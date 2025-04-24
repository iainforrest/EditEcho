package com.example.editecho.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.editecho.ui.components.ToneButton
import com.example.editecho.ui.theme.EditEchoColors
import com.example.editecho.view.EditEchoOverlayViewModel
import com.example.editecho.view.RecordingState
import com.example.editecho.view.ToneState
import kotlinx.coroutines.launch
import java.io.File
import android.content.res.Configuration
import com.example.editecho.util.AudioRecorder
import com.example.editecho.view.EditEchoViewModelFactory
import com.example.editecho.prompt.ToneProfile

/**
 * A bottom sheet overlay that provides audio recording, transcription, and tone adjustment functionality.
 *
 * @param onDismiss Callback to be invoked when the overlay is dismissed.
 */
@Composable
fun EditEchoOverlay(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EditEchoOverlayViewModel = viewModel(
        factory = EditEchoViewModelFactory(context)
    )
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val toneState by viewModel.toneState.collectAsStateWithLifecycle()
    val selectedTone = viewModel.selectedTone
    
    val scope = rememberCoroutineScope()
    
    // State variables
    var hasMicPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Remove redundant state variables that are now in ViewModel
    val isRecording = recordingState is RecordingState.Recording
    val isProcessing = recordingState is RecordingState.Processing
    var transcribedText by remember { mutableStateOf("") }
    
    // Update transcribedText when toneState changes
    LaunchedEffect(toneState) {
        transcribedText = when (toneState) {
            is ToneState.Success -> (toneState as ToneState.Success).text
            else -> transcribedText
        }
    }
    
    // Audio recorder
    val audioRecorder = remember { AudioRecorder(context) }
    
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
    
    // Copy text to clipboard
    fun copyTextToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Transcribed Text", transcribedText)
        clipboard.setPrimaryClip(clip)
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
                    .fillMaxHeight(0.7f)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable(enabled = false) { },
                colors = CardDefaults.cardColors(
                    containerColor = EditEchoColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EditEcho",
                            style = MaterialTheme.typography.titleLarge,
                            color = EditEchoColors.PrimaryText
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = EditEchoColors.PrimaryText
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Text field for transcribed text
                    OutlinedTextField(
                        value = transcribedText,
                        onValueChange = { transcribedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = {
                            Text(
                                text = if (isProcessing) "Processing audio..."
                                else "Your transcribed text will appear here",
                                color = EditEchoColors.PrimaryText.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = EditEchoColors.PrimaryText,
                            unfocusedTextColor = EditEchoColors.PrimaryText,
                            focusedBorderColor = EditEchoColors.Primary,
                            unfocusedBorderColor = EditEchoColors.PrimaryText.copy(alpha = 0.6f),
                            focusedPlaceholderColor = EditEchoColors.PrimaryText.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = EditEchoColors.PrimaryText.copy(alpha = 0.6f)
                        ),
                        enabled = !isProcessing
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tone selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToneButton(
                            text = ToneProfile.SMS.displayName,
                            isActive = selectedTone == ToneProfile.SMS,
                            onClick = { viewModel.setTone(ToneProfile.SMS) },
                            modifier = Modifier.weight(1f)
                        )
                        ToneButton(
                            text = ToneProfile.Email.displayName,
                            isActive = selectedTone == ToneProfile.Email,
                            onClick = { viewModel.setTone(ToneProfile.Email) },
                            modifier = Modifier.weight(1f)
                        )
                        ToneButton(
                            text = ToneProfile.Professional.displayName,
                            isActive = selectedTone == ToneProfile.Professional,
                            onClick = { viewModel.setTone(ToneProfile.Professional) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Record button
                        Button(
                            onClick = { if (isRecording) stopRecording() else startRecording() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) EditEchoColors.Error
                                else EditEchoColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording"
                                else "Start Recording"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRecording) "Stop"
                                else "Record"
                            )
                        }
                        
                        // Copy button
                        Button(
                            onClick = { copyTextToClipboard() },
                            enabled = transcribedText.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EditEchoColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Copy")
                        }
                        
                        // Settings button
                        Button(
                            onClick = { /* TODO: Open settings */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EditEchoColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Settings")
                        }
                    }
                }
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

@Preview(showBackground = true)
@Composable
fun EditEchoOverlayPreview() {
    EditEchoOverlay(onDismiss = {})
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditEchoOverlayDarkPreview() {
    EditEchoOverlay(onDismiss = {})
} 