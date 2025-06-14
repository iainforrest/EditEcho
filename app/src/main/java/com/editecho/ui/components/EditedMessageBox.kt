package com.editecho.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.editecho.view.RecordingState
import com.editecho.view.ToneState
import com.editecho.ui.theme.EditEchoColors

/**
 * A composable that displays the edited message with a sound wave animation during recording/processing states.
 *
 * @param recordingState The current recording state
 * @param toneState The current tone processing state
 * @param isTranscribing Whether transcription is currently active
 * @param editedText The final edited text to display
 * @param modifier Optional modifier for the box
 */
@Composable
fun EditedMessageBox(
    recordingState: RecordingState,
    toneState: ToneState,
    isTranscribing: Boolean,
    editedText: String,
    modifier: Modifier = Modifier
) {
    // Generate wave frames
    val waveFrames = remember {
        listOf(
            ")     ", " )    ", "  )   ", "   )  ", "    ) ", "     )",
            "    ( ", "   (  ", "  (   ", " (    ", "(     "
        )
    }
    
    // Animation state
    var frameIdx by remember { mutableStateOf(0) }
    
    // Animation effect
    LaunchedEffect(recordingState, toneState, isTranscribing) {
        while (true) {
            when {
                recordingState is RecordingState.Recording || 
                isTranscribing || 
                recordingState is RecordingState.Processing || 
                toneState is ToneState.Processing -> {
                    delay(100)
                    frameIdx = (frameIdx + 1) % waveFrames.size
                }
                else -> break
            }
        }
    }
    
    // Determine display text based on state
    val displayText = when {
        recordingState is RecordingState.Recording && isTranscribing -> {
            // Show both Recording and Transcribing on separate lines
            "Recording ${waveFrames[frameIdx]}\nTranscribing ${waveFrames[frameIdx]}"
        }
        recordingState is RecordingState.Recording -> "Recording ${waveFrames[frameIdx]}"
        recordingState is RecordingState.Processing && isTranscribing -> "Transcribing ${waveFrames[frameIdx]}"
        recordingState is RecordingState.Processing -> "Processing ${waveFrames[frameIdx]}"
        toneState is ToneState.Processing -> "Editing ${waveFrames[frameIdx]}"
        toneState is ToneState.Success && editedText.isNotEmpty() -> editedText
        else -> "Your edited message will appear here..."
    }
    
    // Determine if we're showing the final text
    val isFinalText = toneState is ToneState.Success && editedText.isNotEmpty()
    
    OutlinedTextField(
        value = displayText,
        onValueChange = { /* Read-only */ },
        readOnly = true,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 300.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Start,
            color = if (isFinalText) EditEchoColors.PrimaryText else EditEchoColors.SecondaryText
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = if (isFinalText) EditEchoColors.PrimaryText else EditEchoColors.SecondaryText,
            focusedTextColor = if (isFinalText) EditEchoColors.PrimaryText else EditEchoColors.SecondaryText,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        maxLines = Int.MAX_VALUE
    )
} 