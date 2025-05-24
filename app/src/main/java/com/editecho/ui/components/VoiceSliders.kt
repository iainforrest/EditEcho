package com.editecho.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editecho.ui.theme.EditEchoColors

/**
 * A composable that displays dual sliders for formality and polish voice settings.
 *
 * @param formality The current formality level (1-5).
 * @param polish The current polish level (1-5).
 * @param onFormalityChange Callback function called when formality changes.
 * @param onPolishChange Callback function called when polish changes.
 * @param modifier Optional modifier for the voice sliders.
 */
@Composable
fun VoiceSliders(
    formality: Int,
    polish: Int,
    onFormalityChange: (Int) -> Unit,
    onPolishChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Formality Slider Section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Formality Label
            Text(
                text = "Formality",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = EditEchoColors.PrimaryText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Formality Slider
            Slider(
                value = formality.toFloat(),
                onValueChange = { newValue ->
                    val intValue = newValue.toInt()
                    if (intValue != formality) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFormalityChange(intValue)
                    }
                },
                valueRange = 1f..5f,
                steps = 3, // 4 intermediate steps between 1-5 gives us discrete 1,2,3,4,5
                colors = SliderDefaults.colors(
                    thumbColor = EditEchoColors.Primary,
                    activeTrackColor = EditEchoColors.Primary,
                    inactiveTrackColor = EditEchoColors.ToneButtonInactive
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Formality Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Casual",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditEchoColors.SecondaryText,
                    fontSize = 10.sp
                )
                Text(
                    text = "Formal",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditEchoColors.SecondaryText,
                    fontSize = 10.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Polish Slider Section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Polish Label
            Text(
                text = "Polish",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = EditEchoColors.PrimaryText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Polish Slider
            Slider(
                value = polish.toFloat(),
                onValueChange = { newValue ->
                    val intValue = newValue.toInt()
                    if (intValue != polish) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPolishChange(intValue)
                    }
                },
                valueRange = 1f..5f,
                steps = 3, // 4 intermediate steps between 1-5 gives us discrete 1,2,3,4,5
                colors = SliderDefaults.colors(
                    thumbColor = EditEchoColors.Primary,
                    activeTrackColor = EditEchoColors.Primary,
                    inactiveTrackColor = EditEchoColors.ToneButtonInactive
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Polish Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Raw",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditEchoColors.SecondaryText,
                    fontSize = 10.sp
                )
                Text(
                    text = "Refined",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditEchoColors.SecondaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceSlidersPreview() {
    VoiceSliders(
        formality = 3,
        polish = 3,
        onFormalityChange = {},
        onPolishChange = {},
        modifier = Modifier.padding(16.dp)
    )
} 