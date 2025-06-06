package com.editecho.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.editecho.R
import com.editecho.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service that displays a persistent notification for EditEcho.
 * When tapped, the notification starts the OverlayService to show the keyboard-style overlay.
 */
@AndroidEntryPoint
class NotificationService : Service() {
    companion object {
        private const val TAG = "NotificationService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "EditEchoChannel"
        private const val CHANNEL_NAME = "EditEcho Service"
        private const val CHANNEL_DESCRIPTION = "Keeps EditEcho running in the background"
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
        // Start foreground immediately with a basic notification
        val notification = createNotificationBuilder(this).build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        // Return START_STICKY to ensure the service is restarted if it's killed by the system
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved - App was killed, restarting service")
        // Restart the service when the app is killed
        val restartServiceIntent = Intent(applicationContext, NotificationService::class.java)
        startForegroundService(restartServiceIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        // Restart the service when it's destroyed
        val restartServiceIntent = Intent(applicationContext, NotificationService::class.java)
        startForegroundService(restartServiceIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationBuilder(context: Context): NotificationCompat.Builder {
        // Create an intent that will start the OverlayService
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START_OVERLAY
        }
        
        // Create a pending intent for the notification
        val pendingIntent = PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Determine notification text based on build variant
        val title = if (BuildConfig.FLAVOR_NAME == "dev") {
            "EditEcho-dev"
        } else {
            "EditEcho"
        }
        
        val contentText = if (BuildConfig.FLAVOR_NAME == "dev") {
            "Tap to open EditEcho overlay (dev)"
        } else {
            "Tap to open EditEcho overlay"
        }
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
    }
} 