package com.editecho.ui.screens

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
import com.editecho.ui.components.ToneButton
import com.editecho.ui.components.TonePicker
import com.editecho.ui.theme.EditEchoColors
import com.editecho.view.EditEchoOverlayViewModel
import com.editecho.view.RecordingState
import com.editecho.view.ToneState
import kotlinx.coroutines.launch
import java.io.File
import android.content.res.Configuration
import com.editecho.util.AudioRecorder
import com.editecho.prompt.ToneProfile
import android.widget.Toast
import android.util.Log

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
    val selectedTone by viewModel.selectedTone.collectAsState()
    
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
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val formattedText = viewModel.formatExample()
            Log.d("EditEchoOverlay", "Copying to clipboard (${formattedText.length} chars):\n$formattedText")
            val clip = android.content.ClipData.newPlainText("EditEcho Text", formattedText)
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
                    ) {
                        // Transcription box
                        Text(
                            text = "Transcription:",
                            style = MaterialTheme.typography.titleMedium,
                            color = EditEchoColors.PrimaryText
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.3f)
                        ) {
                            OutlinedTextField(
                                value = transcribedText,
                                onValueChange = { /* Read-only */ },
                                readOnly = true,
                                placeholder = { Text("Your transcribed text will appear here...") },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.5
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = EditEchoColors.SecondaryText,
                                    focusedTextColor = EditEchoColors.SecondaryText,
                                    unfocusedBorderColor = EditEchoColors.Primary.copy(alpha = 0.5f),
                                    focusedBorderColor = EditEchoColors.Primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Refined text box
                        Text(
                            text = "Edited Message:",
                            style = MaterialTheme.typography.titleMedium,
                            color = EditEchoColors.PrimaryText
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.3f)
                        ) {
                            OutlinedTextField(
                                value = refinedText,
                                onValueChange = { /* Read-only */ },
                                readOnly = true,
                                placeholder = { Text("Your edited message will appear here...") },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.5
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = EditEchoColors.SecondaryText,
                                    focusedTextColor = EditEchoColors.SecondaryText,
                                    unfocusedBorderColor = EditEchoColors.Primary.copy(alpha = 0.5f),
                                    focusedBorderColor = EditEchoColors.Primary
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tone selection using the new TonePicker component
                    TonePicker(
                        selectedTone = selectedTone,
                        onToneSelected = viewModel::onToneSelected
                    )
                    
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
                            onClick = { 
                                copyTextToClipboard()
                                viewModel.saveExample()
                            },
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