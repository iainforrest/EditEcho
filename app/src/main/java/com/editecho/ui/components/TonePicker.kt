package com.editecho.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editecho.prompt.ToneProfile

/**
 * A composable that displays a dropdown menu for tone selection with description.
 *
 * @param selectedTone The currently selected tone.
 * @param onToneSelected Callback function to be called when a tone is selected.
 * @param modifier Optional modifier for the tone picker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TonePicker(
    selectedTone: ToneProfile,
    onToneSelected: (ToneProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        // Dropdown menu (45% width)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(0.45f)
        ) {
            TextField(
                value = selectedTone.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ToneProfile.values().forEach { tone ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = tone.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        onClick = {
                            onToneSelected(tone)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Spacer between dropdown and description (5% width)
        Spacer(modifier = Modifier.width(16.dp))
        
        // Tone description (45% width)
        Text(
            text = selectedTone.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.45f)
        )
    }
} 