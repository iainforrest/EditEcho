package com.editecho.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editecho.prompt.ToneProfile

/**
 * Voice Engine 3.0 integrated panel showing tone selection and polish adjustment
 * 
 * This component demonstrates the proper integration between TonePicker and PolishSlider,
 * ensuring that micro-label updates are triggered when tone selection changes.
 *
 * @param selectedTone Currently selected tone profile
 * @param polishLevel Current polish level (0-100)
 * @param onToneSelected Callback when tone is selected
 * @param onPolishChanged Callback when polish level changes
 * @param modifier Optional modifier
 */
@Composable
fun VoiceEngine3Panel(
    selectedTone: ToneProfile,
    polishLevel: Int,
    onToneSelected: (ToneProfile) -> Unit,
    onPolishChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Panel title
            Text(
                text = "Voice Engine 3.0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Tone selection section
            TonePicker(
                selectedTone = selectedTone,
                onToneSelected = { newTone ->
                    // When tone changes, this triggers micro-label updates automatically
                    // because PolishSlider will recompose with the new selectedTone
                    onToneSelected(newTone)
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            // Polish adjustment section
            PolishSlider(
                selectedTone = selectedTone, // This parameter ensures micro-labels update when tone changes
                polishLevel = polishLevel,
                onPolishChanged = onPolishChanged
            )
            
            // Real-time feedback section
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Tone: ${selectedTone.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Polish: ${selectedTone.lowMicroLabel} → ${selectedTone.highMicroLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Stateful version of VoiceEngine3Panel for demonstration and testing
 * This shows the complete integration with automatic micro-label updates
 */
@Composable
fun VoiceEngine3PanelDemo() {
    var selectedTone by remember { mutableStateOf(ToneProfile.NEUTRAL) }
    var polishLevel by remember { mutableIntStateOf(50) }
    
    VoiceEngine3Panel(
        selectedTone = selectedTone,
        polishLevel = polishLevel,
        onToneSelected = { newTone ->
            selectedTone = newTone
            // Micro-labels in PolishSlider will automatically update
            // because it will recompose with the new selectedTone
        },
        onPolishChanged = { newPolish ->
            polishLevel = newPolish
        }
    )
}

/**
 * Test component to verify micro-label updates work correctly
 * This demonstrates that changing tone triggers automatic micro-label updates
 */
@Composable
fun MicroLabelUpdateTest() {
    var selectedTone by remember { mutableStateOf(ToneProfile.CASUAL) }
    var polishLevel by remember { mutableIntStateOf(30) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Micro-Label Update Test",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Quick tone selector buttons for testing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToneProfile.values().forEach { tone ->
                Button(
                    onClick = { 
                        selectedTone = tone
                        // This should trigger micro-label updates in PolishSlider below
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tone.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // PolishSlider should show updated micro-labels when tone changes above
        PolishSlider(
            selectedTone = selectedTone,
            polishLevel = polishLevel,
            onPolishChanged = { polishLevel = it }
        )
        
        // Visual feedback to confirm updates are working
        Text(
            text = "Current micro-labels: ${selectedTone.lowMicroLabel} ↔ ${selectedTone.highMicroLabel}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
} 