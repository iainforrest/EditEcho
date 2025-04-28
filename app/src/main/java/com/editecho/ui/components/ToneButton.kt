package com.editecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.editecho.ui.theme.EditEchoColors

/**
 * A button component for selecting different tones in the EditEcho app.
 *
 * @param text The text to display on the button.
 * @param isActive Whether the button is currently selected.
 * @param onClick Callback function to be called when the button is clicked.
 * @param modifier Optional modifier for the button.
 */
@Composable
fun ToneButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) EditEchoColors.Primary
                else EditEchoColors.Surface
            )
            .border(
                width = 1.dp,
                color = if (isActive) EditEchoColors.Primary
                else EditEchoColors.Primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) Color.White
            else EditEchoColors.PrimaryText,
            textAlign = TextAlign.Center
        )
    }
} 