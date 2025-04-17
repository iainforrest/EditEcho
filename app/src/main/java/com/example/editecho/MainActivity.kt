package com.example.editecho

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.editecho.service.NotificationService
import android.content.res.Configuration

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = EditEchoColors.Primary,
    secondary = EditEchoColors.Secondary,
    tertiary = EditEchoColors.Accent,
    background = EditEchoColors.Background,
    surface = EditEchoColors.Surface,
    error = EditEchoColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = EditEchoColors.PrimaryText,
    onSurface = EditEchoColors.PrimaryText,
    onError = Color.White
)

// Dark theme color scheme (for future use)
private val DarkColorScheme = darkColorScheme(
    primary = EditEchoColors.Primary,
    secondary = EditEchoColors.Secondary,
    tertiary = EditEchoColors.Accent,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = EditEchoColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun EditEchoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    
    // State to track if the overlay should be shown
    private var showOverlay by mutableStateOf(false)
    
    // Activity result launcher for the RECORD_AUDIO permission
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Audio permission granted, start the notification service
            startNotificationService()
        } else {
            // Permission denied, show message and handle gracefully
            Toast.makeText(this, "Microphone permission is required for audio recording", Toast.LENGTH_LONG).show()
            // Close the app
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if the overlay should be shown (from notification tap)
        showOverlay = intent.getBooleanExtra("show_overlay", false)
        
        // Check if the app has permission to record audio
        checkAudioPermission()
        
        // Set the content of the activity
        setContent {
            EditEchoTheme {
                MainActivityContent(showOverlay = showOverlay)
            }
        }
    }
    
    /**
     * Checks if the app has permission to record audio.
     */
    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, start the notification service
                startNotificationService()
            }
            else -> {
                // Request permission to record audio
                requestAudioPermission()
            }
        }
    }
    
    /**
     * Requests permission to record audio.
     */
    private fun requestAudioPermission() {
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    /**
     * Starts the notification service.
     */
    private fun startNotificationService() {
        val serviceIntent = Intent(this, NotificationService::class.java)
        startForegroundService(serviceIntent)
    }
}

@Composable
fun MainActivityContent(showOverlay: Boolean = false) {
    // State to track if the overlay should be shown
    var isOverlayVisible by remember { mutableStateOf(showOverlay) }
    
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Show a simple message that the app is running
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "EditEcho is running",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "The notification service is active.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { isOverlayVisible = true }
            ) {
                Text("Show EditEcho Overlay")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Close the app */ }
            ) {
                Text("Close App")
            }
        }
        
        // Show the EditEchoOverlay as a bottom sheet if isOverlayVisible is true
        if (isOverlayVisible) {
            EditEchoOverlay(
                onDismiss = { isOverlayVisible = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    EditEchoTheme {
        MainActivityContent()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainActivityDarkPreview() {
    EditEchoTheme(darkTheme = true) {
        MainActivityContent()
    }
} 