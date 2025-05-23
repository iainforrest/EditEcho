package com.editecho

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.editecho.service.NotificationService
import com.editecho.ui.theme.EditEchoColors
import android.content.res.Configuration
import com.editecho.ui.screens.EditEchoOverlay
import com.editecho.ui.theme.EditEchoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // State to track if the overlay should be shown
    private var showOverlay by mutableStateOf(false)
    
    // Activity result launcher for the RECORD_AUDIO permission
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Audio permission granted, check notification permission
            checkNotificationPermission()
        } else {
            // Permission denied, show message and handle gracefully
            Toast.makeText(this, "Microphone permission is required for audio recording", Toast.LENGTH_LONG).show()
            // Close the app
            finish()
        }
    }

    // Activity result launcher for the POST_NOTIFICATIONS permission
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Notification permission granted, start the notification service
            startNotificationService()
        } else {
            // Permission denied, show message
            Toast.makeText(this, "Notification permission is required for EditEcho to work properly", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log the API key from BuildConfig for verification
        Log.d("MainActivity", "🔑 Using API key from BuildConfig: ${BuildConfig.OPENAI_API_KEY.take(10)}...")
        
        // Check if the overlay should be shown (from notification tap)
        showOverlay = intent.getBooleanExtra("show_overlay", false)
        
        // Check if the app has permission to record audio
        checkAudioPermission()
        
        // Set the content of the activity
        setContent {
            EditEchoTheme {
                MainActivityContent(
                    showOverlay = showOverlay,
                    onDismissOverlay = { showOverlay = false }
                )
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
                // Permission already granted, check notification permission
                checkNotificationPermission()
            }
            else -> {
                // Request the permission
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    /**
     * Checks if the app has permission to post notifications.
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, start the notification service
                    startNotificationService()
                }
                else -> {
                    // Request the permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, no need to request notification permission
            startNotificationService()
        }
    }
    
    /**
     * Starts the notification service.
     */
    private fun startNotificationService() {
        val serviceIntent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    
    /**
     * Shows the EditEcho overlay.
     */
    private fun showEditEchoOverlay() {
        showOverlay = true
    }
    
    /**
     * Hides the EditEcho overlay.
     */
    private fun hideEditEchoOverlay() {
        showOverlay = false
    }
}

/**
 * The main content of the MainActivity.
 */
@Composable
fun MainActivityContent(
    showOverlay: Boolean,
    onDismissOverlay: () -> Unit
) {
    // Surface using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showOverlay) {
            // Show the EditEcho overlay
            EditEchoOverlay(onDismiss = onDismissOverlay)
        } else {
            // Show the main screen
            MainScreen()
        }
    }
}

/**
 * The main screen of the app.
 */
@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EditEcho",
            style = MaterialTheme.typography.headlineLarge,
            color = EditEchoColors.Primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tap the notification to start recording",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Preview for the MainScreen.
 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    EditEchoTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainActivityDarkPreview() {
    EditEchoTheme(darkTheme = true) {
        MainActivityContent(
            showOverlay = false,
            onDismissOverlay = {}
        )
    }
} 