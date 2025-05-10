package com.editecho.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A horizontal row of tone buttons for selecting the tone of the text.
 *
 * @param selectedTone The currently selected tone.
 * @param onToneSelected The callback to be invoked when a tone is selected.
 * @param modifier The modifier to be applied to the row.
 */
@Composable
fun ToneSelector(
    selectedTone: String,
    onToneSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val tones = listOf("Friendly", "Engaged", "Direct", "Reflective")
        tones.forEach { tone ->
            ToneButton(
                text = tone,
                isActive = tone == selectedTone,
                onClick = { onToneSelected(tone) },
                modifier = Modifier.weight(1f)
            )
        }
    }
} 