package com.editecho

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EditEchoApplication : Application() {
    companion object {
        private const val TAG = "EditEchoApplication"
    }

    @Inject
    lateinit var notificationManager: NotificationManager
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase Analytics
        analytics = FirebaseAnalytics.getInstance(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application onTerminate")
    }
} 