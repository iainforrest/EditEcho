package com.editecho.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import com.editecho.R
import com.editecho.ui.screens.EditEchoOverlayContent
import com.editecho.ui.theme.EditEchoColors
import com.editecho.view.EditEchoOverlayViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android Foreground Service that manages a WindowManager-based overlay for EditEcho.
 * This service creates and manages a keyboard-style overlay positioned at the bottom of the screen.
 * 
 * OVERLAY BEHAVIOR DOCUMENTATION:
 * 
 * The overlay is designed to behave like a system keyboard and will interact differently with various app types:
 * 
 * 1. STANDARD APPS (messaging, email, browsers, note-taking):
 *    - Underlying app content will resize/pan up to accommodate the overlay
 *    - EditEcho overlay appears at bottom with 250dp height
 *    - Touch events pass through to underlying app where not intercepted by overlay
 *    - Apps maintain focus and continue normal operation
 * 
 * 2. FULL-SCREEN APPS (games, video players, immersive apps):
 *    - Overlay will appear on top without resizing the underlying content
 *    - Some full-screen apps may ignore SOFT_INPUT_ADJUST_RESIZE
 *    - Overlay remains functional but may partially cover app content
 *    - Use FLAG_ALT_FOCUSABLE_IM to ensure overlay stays above system keyboard
 * 
 * 3. SPLIT-SCREEN MODE:
 *    - Overlay appears in the active split-screen section
 *    - Behavior follows the focused app's input handling characteristics
 *    - Height remains fixed at 250dp regardless of split dimensions
 * 
 * 4. SYSTEM KEYBOARD INTERACTION:
 *    - When system keyboard appears, EditEcho overlay remains above it
 *    - SOFT_INPUT_ADJUST_RESIZE ensures proper content adjustment
 *    - FLAG_ALT_FOCUSABLE_IM prevents overlay from being pushed down
 *    - Apps that don't respond to soft input won't resize for either keyboard
 * 
 * 5. EDGE CASES:
 *    - Apps with custom input handling may not resize appropriately
 *    - Overlay functionality (recording, transcription, editing) works regardless
 *    - Close button always accessible to dismiss overlay
 *    - Service can be stopped via notification or close button
 */
@AndroidEntryPoint
class OverlayService : Service(), ViewModelStoreOwner {

    companion object {
        private const val TAG = "OverlayService"
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 2001
        
        const val ACTION_START_OVERLAY = "com.editecho.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.editecho.STOP_OVERLAY"
        
        // Keyboard-like height (approximately 250dp converted to pixels will be calculated at runtime)
        private const val OVERLAY_HEIGHT_DP = 250
    }

    internal lateinit var windowManager: WindowManager
    internal lateinit var notificationManager: NotificationManager
    private var overlayView: ComposeView? = null

    lateinit var editEchoOverlayViewModel: EditEchoOverlayViewModel

    // ViewModelStore implementation
    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayService created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()

        // Initialize ViewModel using ViewModelProvider with Hilt
        editEchoOverlayViewModel = ViewModelProvider(this)[EditEchoOverlayViewModel::class.java]
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_OVERLAY -> {
                startForeground(NOTIFICATION_ID, createNotification())
                if (hasOverlayPermission()) {
                    showOverlay()
                } else {
                    Log.e(TAG, "SYSTEM_ALERT_WINDOW permission not granted")
                    handlePermissionNotGranted()
                }
            }
            ACTION_STOP_OVERLAY -> {
                hideOverlay()
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OverlayService destroyed")
        hideOverlay()
        viewModelStore.clear()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Checks if the app has the SYSTEM_ALERT_WINDOW permission.
     * This is required for TYPE_APPLICATION_OVERLAY windows.
     */
    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            // Permission is automatically granted on devices below API 23
            true
        }
    }

    /**
     * Handles the case where SYSTEM_ALERT_WINDOW permission is not granted.
     * This sends a notification to guide the user to enable the permission.
     */
    internal fun handlePermissionNotGranted() {
        Log.w(TAG, "Overlay permission not granted. Guiding user to settings.")
        
        // Create an intent to open the overlay permission settings
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        // Send a notification to inform the user
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EditEcho Permission Required")
            .setContentText("Please enable 'Display over other apps' permission for EditEcho overlay")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    this, 
                    0, 
                    intent, 
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
            
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
        
        // Stop the service since we can't show the overlay
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Overlay Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Service for EditEcho overlay"
            setShowBadge(false)
        }
        
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EditEcho Overlay Active")
            .setContentText("EditEcho overlay is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    internal fun showOverlay() {
        Log.d(TAG, "showOverlay() called")
        
        if (overlayView != null) {
            Log.d(TAG, "Overlay already showing")
            return
        }
        
        // Double-check permission before showing overlay
        if (!hasOverlayPermission()) {
            Log.e(TAG, "Cannot show overlay: permission not granted")
            handlePermissionNotGranted()
            return
        }
        
        try {
            // Create ComposeView for hosting Jetpack Compose content
            overlayView = ComposeView(this).apply {
                // Set up ViewModelStore for this ComposeView
                setViewTreeViewModelStoreOwner(this@OverlayService)
                
                // Set the Compose content with EditEchoOverlayContent
                setContent {
                    // Collect state from ViewModel
                    val recordingState by editEchoOverlayViewModel.recordingState.collectAsState()
                    val toneState by editEchoOverlayViewModel.toneState.collectAsState()
                    val voiceSettings by editEchoOverlayViewModel.voiceSettings.collectAsState()
                    val refinedText by editEchoOverlayViewModel.refinedText.collectAsState()
                    
                    // Create a Card container with proper styling for the overlay
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = EditEchoColors.Surface
                        )
                    ) {
                        EditEchoOverlayContent(
                            recordingState = recordingState,
                            toneState = toneState,
                            voiceSettings = voiceSettings,
                            refinedText = refinedText,
                            onDismiss = { 
                                // Stop the service when close button is pressed
                                val intent = Intent(this@OverlayService, OverlayService::class.java)
                                intent.action = ACTION_STOP_OVERLAY
                                startService(intent)
                            },
                            onFormalityChanged = editEchoOverlayViewModel::onFormalityChanged,
                            onPolishChanged = editEchoOverlayViewModel::onPolishChanged,
                            onStartRecording = { editEchoOverlayViewModel.startRecording() },
                            onStopRecording = { editEchoOverlayViewModel.stopRecording() },
                            onCopyToClipboard = { 
                                // Copy functionality will be handled by ViewModel or inline
                                try {
                                    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("EditEcho Text", refinedText)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(this@OverlayService, "Text copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to copy to clipboard", e)
                                    android.widget.Toast.makeText(this@OverlayService, "Failed to copy text", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
            
            // Add the ComposeView to WindowManager with proper layout params
            windowManager.addView(overlayView, createLayoutParams())
            Log.d(TAG, "Overlay added to WindowManager")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay", e)
            overlayView = null
        }
    }

    internal fun hideOverlay() {
        Log.d(TAG, "hideOverlay() called")
        
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
                Log.d(TAG, "Overlay removed from WindowManager")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay", e)
            }
            overlayView = null
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        // Convert DP to pixels for keyboard-like height
        val density = resources.displayMetrics.density
        val heightPixels = (OVERLAY_HEIGHT_DP * density).toInt()
        
        return WindowManager.LayoutParams(
            // Width: Match parent to span full screen width
            WindowManager.LayoutParams.MATCH_PARENT,
            // Height: Fixed keyboard-like height
            heightPixels,
            // Type: Application overlay (requires SYSTEM_ALERT_WINDOW permission)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // Flags for proper keyboard-like behavior
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or // Don't steal focus from underlying app
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or // Allow touches outside overlay to pass through
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or // Layout within screen bounds
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or // Allow layout beyond screen
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or // Detect touches outside (but don't consume them)
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, // Remain above soft keyboard when shown
            // Pixel format
            PixelFormat.TRANSLUCENT
        ).apply {
            // Position at bottom of screen like a keyboard
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            // Soft input adjustment: resize underlying app content and keep overlay visible above keyboard
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or 
                          WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        }
    }
} 