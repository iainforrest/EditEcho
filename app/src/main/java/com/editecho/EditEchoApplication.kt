package com.editecho

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EditEchoApplication : Application() {
    companion object {
        private const val TAG = "EditEchoApplication"
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application onTerminate")
    }
} 