package com.example.editecho

import android.app.Application
import android.util.Log
import com.example.editecho.service.NotificationService

class EditEchoApplication : Application() {
    companion object {
        private const val TAG = "EditEchoApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        
        // Create notification channel once at app startup
        NotificationService.createNotificationChannel(this)
    }
} 