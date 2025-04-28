package com.editecho.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting NotificationService")
            context.startForegroundService(
                Intent(context, NotificationService::class.java)
            )
        }
    }
} 