package com.editecho.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editecho.prompt.ToneProfile
import kotlin.math.roundToInt

/**
 * Voice Engine 3.0 polish slider with dynamic micro-labels
 * 
 * The polish slider adapts its labels and behavior based on the selected tone,
 * mapping polish levels (0-100) to tone-specific formality ranges.
 *
 * @param selectedTone The currently selected tone profile
 * @param polishLevel Current polish level (0-100)
 * @param onPolishChanged Callback when polish level changes
 * @param modifier Optional modifier for the slider
 */
@Composable
fun PolishSlider(
    selectedTone: ToneProfile,
    polishLevel: Int,
    onPolishChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        // Horizontal layout: micro-label | slider | micro-label
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left micro-label
            Text(
                text = selectedTone.lowMicroLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            // Slider in the middle
            Slider(
                value = polishLevel.toFloat(),
                onValueChange = { newValue ->
                    onPolishChanged(newValue.roundToInt())
                },
                valueRange = 0f..100f,
                steps = 19, // Creates 20 segments (0, 5, 10, ..., 100)
                modifier = Modifier.weight(1f)
            )
            
            // Right micro-label
            Text(
                text = selectedTone.highMicroLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Preview version of PolishSlider for testing different tones
 */
@Composable
fun PolishSliderPreview(
    tone: ToneProfile = ToneProfile.NEUTRAL,
    initialPolish: Int = 50
) {
    var polishLevel by remember { mutableIntStateOf(initialPolish) }
    
    PolishSlider(
        selectedTone = tone,
        polishLevel = polishLevel,
        onPolishChanged = { polishLevel = it }
    )
} 