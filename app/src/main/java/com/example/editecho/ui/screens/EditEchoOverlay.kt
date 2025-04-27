package com.example.editecho.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.editecho.ui.components.ToneButton
import com.example.editecho.ui.theme.EditEchoColors
import com.example.editecho.view.EditEchoOverlayViewModel
import com.example.editecho.view.RecordingState
import com.example.editecho.view.ToneState
import kotlinx.coroutines.launch
import java.io.File
import android.content.res.Configuration
import com.example.editecho.util.AudioRecorder
import com.example.editecho.prompt.ToneProfile
import android.widget.Toast

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
    val selectedTone = viewModel.selectedTone
    
    // Collect the transcribed and refined text from the ViewModel
    val transcribedText by viewModel.transcribedText.collectAsState()
    val refinedText by viewModel.refinedText.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    // State variables
    var hasMicPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Remove redundant state variables that are now in ViewModel
    val isRecording = recordingState is RecordingState.Recording
    val isProcessing = recordingState is RecordingState.Processing || toneState is ToneState.Processing
    
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
    
    // Copy both transcribed and refined text to clipboard
    fun copyTextToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        
        // Format the text with both sections
        val formattedText = """
            🎙️ Transcription:
            $transcribedText

            ✨ Edited Message:
            $refinedText
        """.trimIndent()
        
        // Copy to clipboard
        val clip = android.content.ClipData.newPlainText("EditEcho Text", formattedText)
        clipboard.setPrimaryClip(clip)
        
        // Show toast confirmation
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
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
                    
                    // Text display area with both transcribed and refined text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (transcribedText.isNotEmpty()) {
                            Text(
                                text = "Transcription:",
                                style = MaterialTheme.typography.titleMedium,
                                color = EditEchoColors.PrimaryText
                            )
                            Text(
                                text = transcribedText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = EditEchoColors.SecondaryText,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (refinedText.isNotEmpty()) {
                            Text(
                                text = "Edited Message:",
                                style = MaterialTheme.typography.titleMedium,
                                color = EditEchoColors.PrimaryText,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Text(
                                text = refinedText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = EditEchoColors.SecondaryText,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tone selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToneButton(
                            text = ToneProfile.QUICK.displayName,
                            isActive = selectedTone == ToneProfile.QUICK,
                            onClick = { viewModel.setTone(ToneProfile.QUICK) },
                            modifier = Modifier.weight(1f)
                        )
                        ToneButton(
                            text = ToneProfile.FRIENDLY.displayName,
                            isActive = selectedTone == ToneProfile.FRIENDLY,
                            onClick = { viewModel.setTone(ToneProfile.FRIENDLY) },
                            modifier = Modifier.weight(1f)
                        )
                        ToneButton(
                            text = ToneProfile.POLISHED.displayName,
                            isActive = selectedTone == ToneProfile.POLISHED,
                            onClick = { viewModel.setTone(ToneProfile.POLISHED) },
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
                            enabled = transcribedText.isNotEmpty() || refinedText.isNotEmpty(),
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