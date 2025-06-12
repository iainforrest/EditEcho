package com.editecho.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editecho.prompt.ToneProfile

/**
 * Voice Engine 3.0 tone picker with formality range display
 *
 * @param selectedTone The currently selected tone profile.
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
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            // Dropdown menu (45% width)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(0.45f)
            ) {
                // Custom dropdown button with minimal padding
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .height(36.dp) // Fixed height instead of heightIn
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 4.dp), // Minimal custom padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedTone.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown arrow",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
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
                                    fontWeight = FontWeight.Medium
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
            Spacer(modifier = Modifier.width(8.dp))
            
            // Tone description and details (45% width)
            Column(
                modifier = Modifier.weight(0.45f)
            ) {
                Text(
                    text = selectedTone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            }
        }
    }
} 