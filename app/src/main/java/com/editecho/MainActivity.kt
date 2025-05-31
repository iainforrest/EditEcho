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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.editecho.network.ClaudeCompletionClient
import com.editecho.service.NotificationService
import com.editecho.ui.theme.EditEchoColors
import com.editecho.ui.theme.EditEchoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var claudeCompletionClient: ClaudeCompletionClient

    // Launcher for requesting audio permission
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, check notification permission
            checkNotificationPermission()
        } else {
            // Permission denied, show message
            Toast.makeText(this, "Audio permission is required for EditEcho to work properly", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher for requesting notification permission (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start the notification service
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
        
        // Check if the app has permission to record audio
        checkAudioPermission()
        
        // Set the content of the activity
        setContent {
            EditEchoTheme {
                MainActivityContent(
                    claudeCompletionClient = claudeCompletionClient
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
}

/**
 * The main content of the MainActivity.
 */
@Composable
fun MainActivityContent(
    claudeCompletionClient: ClaudeCompletionClient? = null
) {
    // Surface using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MainScreen(claudeCompletionClient = claudeCompletionClient)
    }
}

/**
 * The main screen of the app.
 */
@Composable
fun MainScreen(claudeCompletionClient: ClaudeCompletionClient? = null) {
    val scope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf("") }
    var isTestRunning by remember { mutableStateOf(false) }
    
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
        
        if (claudeCompletionClient != null) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isTestRunning = true
                    scope.launch {
                        try {
                            Log.d("MainActivity", "Testing Claude API...")
                            val result = claudeCompletionClient.testComplete()
                            testResult = "✅ Claude API Test Successful!\nResult: ${result.take(100)}..."
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Claude API test failed", e)
                            testResult = "❌ Claude API Test Failed: ${e.message}"
                        } finally {
                            isTestRunning = false
                        }
                    }
                },
                enabled = !isTestRunning
            ) {
                Text(if (isTestRunning) "Testing..." else "Test Claude API")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    Log.d("MainActivity", "🔧 Testing Settings Persistence...")
                    testResult = "Settings persistence test - check logs for details.\n\nTo test:\n1. Open app overlay\n2. Set sliders to 1,5\n3. Close app\n4. Reopen app\n5. Check if sliders are still at 1,5"
                }
            ) {
                Text("Info: Test Settings Persistence")
            }
            
            if (testResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = testResult,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
} 