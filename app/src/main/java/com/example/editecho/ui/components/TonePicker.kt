package com.example.editecho.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.editecho.prompt.ToneProfile

/**
 * A composable that displays a row of tone selection buttons.
 *
 * @param selectedTone The currently selected tone.
 * @param onToneSelected Callback function to be called when a tone is selected.
 * @param modifier Optional modifier for the tone picker.
 */
@Composable
fun TonePicker(
    selectedTone: ToneProfile,
    onToneSelected: (ToneProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Quick tone button
        ToneButton(
            text = ToneProfile.QUICK.displayName,
            isActive = ToneProfile.QUICK == selectedTone,
            onClick = { onToneSelected(ToneProfile.QUICK) },
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        
        // Friendly tone button
        ToneButton(
            text = ToneProfile.FRIENDLY.displayName,
            isActive = ToneProfile.FRIENDLY == selectedTone,
            onClick = { onToneSelected(ToneProfile.FRIENDLY) },
            modifier = Modifier.fillMaxWidth(0.3f)
        )
        
        // Polished tone button
        ToneButton(
            text = ToneProfile.POLISHED.displayName,
            isActive = ToneProfile.POLISHED == selectedTone,
            onClick = { onToneSelected(ToneProfile.POLISHED) },
            modifier = Modifier.fillMaxWidth(0.3f)
        )
    }
} 