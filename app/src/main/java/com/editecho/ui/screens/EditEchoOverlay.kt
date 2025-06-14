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
import com.editecho.ui.components.TonePicker
import com.editecho.ui.components.PolishSlider
import com.editecho.prompt.VoiceSettings
import com.editecho.prompt.ToneProfile

/**
 * Reusable content for the EditEcho overlay that can be used in both Dialog and WindowManager contexts.
 * This contains the main UI elements: header, text display, sliders, and action buttons.
 */
@Composable
fun EditEchoOverlayContent(
    recordingState: RecordingState,
    toneState: ToneState,
    refinedText: String,
    selectedTone: ToneProfile,
    polishLevel: Int,
    onDismiss: () -> Unit,
    onToneSelected: (ToneProfile) -> Unit,
    onPolishLevelChanged: (Int) -> Unit,
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
                        .weight(0.6f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Voice Engine 3.0 Section - Tone-First Interface
                Column(modifier = Modifier.weight(0.4f).padding(bottom = 4.dp)) {
                    // Tone selection
                    TonePicker(
                        selectedTone = selectedTone,
                        onToneSelected = onToneSelected,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    
                    // Polish adjustment with dynamic micro-labels
                    PolishSlider(
                        selectedTone = selectedTone,
                        polishLevel = polishLevel,
                        onPolishChanged = onPolishLevelChanged,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Legacy sliders removed in Voice Engine 3.0
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
    // Legacy voiceSettings removed in Voice Engine 3.0
    val refinedText by viewModel.refinedText.collectAsState()
    
    // Voice Engine 3.0 state
    val selectedTone by viewModel.selectedTone.collectAsState()
    val polishLevel by viewModel.polishLevel.collectAsState()
    
    // State variables
    var hasMicPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
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
                    refinedText = refinedText,
                    selectedTone = selectedTone,
                    polishLevel = polishLevel,
                    onDismiss = onDismiss,
                    onToneSelected = viewModel::onToneSelected,
                    onPolishLevelChanged = viewModel::onPolishLevelChanged,
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