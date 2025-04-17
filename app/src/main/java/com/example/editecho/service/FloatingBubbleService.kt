package com.example.editecho.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.editecho.FloatingBubbleOverlay
import com.example.editecho.R

/**
 * Service that manages the floating bubble overlay.
 * This service creates and manages the system window that contains the floating bubble.
 */
class FloatingBubbleService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner, OnBackPressedDispatcherOwner {
    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    
    // Lifecycle implementation
    private val lifecycleRegistry = LifecycleRegistry(this)
    
    // SavedStateRegistry implementation
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    
    // ViewModelStore implementation
    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = _viewModelStore
    
    // OnBackPressedDispatcher implementation
    private val _onBackPressedDispatcher = OnBackPressedDispatcher()
    override val onBackPressedDispatcher: OnBackPressedDispatcher
        get() = _onBackPressedDispatcher

    companion object {
        private const val TAG = "FloatingBubbleService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "FloatingBubbleChannel"
        private const val CHANNEL_NAME = "Floating Bubble Service"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        
        // Initialize lifecycle
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        // Initialize saved state registry
        savedStateRegistryController.performRestore(Bundle())
        
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        createFloatingView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        
        // Clean up lifecycle
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        
        // Clean up saved state registry
        savedStateRegistryController.performSave(Bundle())
        
        // Clean up view model store
        _viewModelStore.clear()
        
        removeFloatingView()
    }
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("EditEcho")
        .setContentText("Floating bubble is active")
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun createFloatingView() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 100
            }

            // Create a ComposeView and set up the content
            val composeView = ComposeView(this)
            
            // Set up the content with the FloatingBubbleOverlay composable
            composeView.setContent {
                FloatingBubbleOverlay()
            }
            
            // Add the view to the window manager
            windowManager?.addView(composeView, params)
            
            // Store the reference to the floating view
            floatingView = composeView
            
            Log.d(TAG, "Floating view created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating floating view", e)
        }
    }

    private fun removeFloatingView() {
        try {
            floatingView?.let { view ->
                windowManager?.removeView(view)
                floatingView = null
            }
            Log.d(TAG, "Floating view removed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing floating view", e)
        }
    }
}