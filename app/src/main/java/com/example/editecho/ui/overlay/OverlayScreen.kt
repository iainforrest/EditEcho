package com.example.editecho.ui.overlay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.editecho.R

@Composable
fun OverlayScreen(
    viewModel: OverlayViewModel = hiltViewModel()
) {
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isOverlayActive) stringResource(R.string.overlay_active)
                  else stringResource(R.string.overlay_inactive),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.toggleOverlay() },
            enabled = hasOverlayPermission || !isOverlayActive
        ) {
            Text(
                text = if (isOverlayActive) stringResource(R.string.stop_overlay)
                      else stringResource(R.string.start_overlay)
            )
        }

        if (!hasOverlayPermission && !isOverlayActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.overlay_permission_required),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
} 