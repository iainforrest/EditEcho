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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.editecho.ui.components.ToneButton
import com.example.editecho.ui.theme.EditEchoColors
import com.example.editecho.util.AudioRecorder
import com.example.editecho.util.OpenAIService
import com.example.editecho.view.EditEchoOverlayViewModel
import com.example.editecho.view.RecordingState
import com.example.editecho.view.ToneState
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
fun EditEchoOverlay(
    modifier: Modifier = Modifier,
    viewModel: EditEchoOverlayViewModel = viewModel()
) {
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val toneState by viewModel.toneState.collectAsStateWithLifecycle()
    val selectedTone = viewModel.selectedTone
    
    // Determine if we're in a "thinking" state
    val isThinking = recordingState is RecordingState.Processing || toneState is ToneState.Processing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tone selection buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToneButton(
                text = "Professional",
                isActive = selectedTone == "Professional",
                onClick = { viewModel.setTone("Professional") }
            )
            ToneButton(
                text = "Casual",
                isActive = selectedTone == "Casual",
                onClick = { viewModel.setTone("Casual") }
            )
            ToneButton(
                text = "Friendly",
                isActive = selectedTone == "Friendly",
                onClick = { viewModel.setTone("Friendly") }
            )
        }

        // Recording button
        Button(
            onClick = {
                when (recordingState) {
                    is RecordingState.Idle -> viewModel.startRecording()
                    is RecordingState.Recording -> viewModel.stopRecording()
                    else -> {} // Do nothing for other states
                }
            },
            enabled = recordingState !is RecordingState.Processing
        ) {
            Text(
                text = when (recordingState) {
                    is RecordingState.Idle -> "Start Recording"
                    is RecordingState.Recording -> "Stop Recording"
                    is RecordingState.Processing -> "Processing..."
                    is RecordingState.Error -> "Try Again"
                }
            )
        }

        // Main text field - read-only
        when (toneState) {
            is ToneState.Success -> {
                TextField(
                    value = (toneState as ToneState.Success).text,
                    onValueChange = {}, // read-only
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    label = { Text("Refined Text") }
                )
            }
            is ToneState.Error -> {
                Text(
                    text = (toneState as ToneState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                // Placeholder text when no result yet
                TextField(
                    value = "Record audio to see refined text here",
                    onValueChange = {}, // read-only
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    label = { Text("Refined Text") }
                )
            }
        }

        // Linear progress indicator during processing
        if (isThinking) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        // Error display for recording state
        if (recordingState is RecordingState.Error) {
            Text(
                text = (recordingState as RecordingState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditEchoOverlayPreview() {
    MaterialTheme {
        EditEchoOverlay()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditEchoOverlayDarkPreview() {
    MaterialTheme {
        EditEchoOverlay()
    }
} 