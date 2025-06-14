package com.editecho.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.editecho.R
import com.editecho.data.SettingsRepository
import com.editecho.data.VoiceDNARepository
import com.editecho.network.AssistantApiClient
import com.editecho.network.ChatCompletionClient
import com.editecho.network.ClaudeCompletionClient
import com.editecho.network.WhisperRepository
import com.editecho.ui.screens.EditEchoOverlayContent
import com.editecho.ui.theme.EditEchoColors
import com.editecho.view.EditEchoOverlayViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android Foreground Service that manages a WindowManager-based overlay for EditEcho.
 * This service creates and manages a keyboard-style overlay positioned at the bottom of the screen.
 *
 * ## Keyboard and Inset Handling
 *
 * This service uses a `WindowManager` with a `ComposeView` to create the overlay. The keyboard
 * interaction is handled differently based on the Android version:
 *
 * - **API 30+ (Android 11+):** A `setOnApplyWindowInsetsListener` is attached to the `ComposeView`.
 *   This listener detects when the IME (Input Method Editor, i.e., the keyboard) is visible
 *   and calculates its height. This height is then applied as bottom padding to the main `Card`
 *   of the overlay, pushing the UI up to avoid being obscured by the keyboard.
 *
 * - **Below API 30:** The `WindowManager.LayoutParams` use the `SOFT_INPUT_ADJUST_RESIZE` flag,
 *   which is the legacy method for telling the window to resize when the keyboard appears.
 *
 * ### Dropdown Menu Behavior
 *
 * The overlay window uses the `FLAG_NOT_FOCUSABLE` flag. This is necessary to allow users to
 * interact with the application behind the overlay. However, this creates a challenge for
 * dropdown menus, as they typically need to open in a new, focusable window.
 *
 * The standard `ExposedDropdownMenuBox` fails in this context because its popup is often
 * positioned incorrectly or hidden behind the keyboard. To solve this, we use a custom
 * `KeyboardAwareDropdownMenu` component. This component:
 *
 * 1.  Uses a standard `DropdownMenu`, which gives us more control over positioning.
 * 2.  Listens to `WindowInsets.ime` to detect the keyboard's height.
 * 3.  Manually calculates and applies a negative vertical offset to the dropdown, effectively
 *     shifting it upwards to stay visible above the keyboard.
 *
 * This approach ensures the dropdown remains usable without interfering with the non-focusable
 * nature of the main overlay window.
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
class OverlayService : Service(), ViewModelStoreOwner, LifecycleOwner, SavedStateRegistryOwner {

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
    private var windowContext: Context? = null

    @Inject lateinit var whisperRepo: WhisperRepository
    @Inject lateinit var assistant: AssistantApiClient
    @Inject lateinit var chatCompletionClient: ChatCompletionClient
    @Inject lateinit var claudeCompletionClient: ClaudeCompletionClient
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var voiceDNARepository: VoiceDNARepository

    lateinit var editEchoOverlayViewModel: EditEchoOverlayViewModel

    // ViewModelStore implementation
    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayService created")

        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayManager = getSystemService(DisplayManager::class.java)
            val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            windowContext = createWindowContext(
                defaultDisplay,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                null
            )
        }

        editEchoOverlayViewModel = EditEchoOverlayViewModel(
            context = applicationContext,
            whisperRepo = whisperRepo,
            assistant = assistant,
            chatCompletionClient = chatCompletionClient,
            claudeCompletionClient = claudeCompletionClient,
            settings = settings,
            voiceDNARepository = voiceDNARepository
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        Log.d(TAG, "✅ OverlayService is the active implementation")
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
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
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
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
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            val contextToUse = windowContext ?: this
            val viewDensity = resources.displayMetrics.density
            
            overlayView = ComposeView(contextToUse).apply {
                setViewTreeLifecycleOwner(this@OverlayService)
                setViewTreeViewModelStoreOwner(this@OverlayService)
                setViewTreeSavedStateRegistryOwner(this@OverlayService)
                
                var imePadding by mutableStateOf(0.dp)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setOnApplyWindowInsetsListener { _, insets ->
                        val imeVisible = insets.isVisible(WindowInsets.Type.ime())
                        val imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom
                        imePadding = if (imeVisible) (imeHeight / viewDensity).dp else 0.dp
                        Log.d(TAG, "IME visible: $imeVisible, height: $imeHeight, padding: $imePadding")
                        insets.inset(0, 0, 0, if (imeVisible) imeHeight else 0)
                    }
                }
                
                setContent {
                    val recordingState by editEchoOverlayViewModel.recordingState.collectAsState()
                    val toneState by editEchoOverlayViewModel.toneState.collectAsState()
                    val refinedText by editEchoOverlayViewModel.refinedText.collectAsState()
                    
                    // Voice Engine 3.0 state
                    val selectedTone by editEchoOverlayViewModel.selectedTone.collectAsState()
                    val polishLevel by editEchoOverlayViewModel.polishLevel.collectAsState()
                    
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .padding(bottom = imePadding),
                        colors = CardDefaults.cardColors(containerColor = EditEchoColors.Surface)
                    ) {
                        EditEchoOverlayContent(
                            recordingState = recordingState,
                            toneState = toneState,
                            refinedText = refinedText,
                            selectedTone = selectedTone,
                            polishLevel = polishLevel,
                            onDismiss = { 
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(this@OverlayService, OverlayService::class.java)
                                    intent.action = ACTION_STOP_OVERLAY
                                    startService(intent)
                                }, 100)
                            },
                            onToneSelected = editEchoOverlayViewModel::onToneSelected,
                            onPolishLevelChanged = editEchoOverlayViewModel::onPolishLevelChanged,
                            onStartRecording = { editEchoOverlayViewModel.startRecording() },
                            onStopRecording = { editEchoOverlayViewModel.stopRecording() },
                            onCopyToClipboard = { 
                                try {
                                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
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
        if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val density = resources.displayMetrics.density
        val heightPixels = (OVERLAY_HEIGHT_DP * density).toInt()
        val overlayFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                         WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                         WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            heightPixels,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            overlayFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                              WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
                Log.d(TAG, "Using legacy soft input mode for keyboard handling")
            } else {
                Log.d(TAG, "Relying on WindowInsets API for keyboard handling (API 30+)")
            }
        }
    }
} 