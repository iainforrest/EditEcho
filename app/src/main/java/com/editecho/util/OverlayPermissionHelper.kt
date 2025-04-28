package com.editecho.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

/**
 * Utility class to handle overlay permission requests.
 */
object OverlayPermissionHelper {
    private const val TAG = "OverlayPermissionHelper"
    
    /**
     * Checks if the app has permission to draw overlays.
     *
     * @param context The application context.
     * @return true if the app has permission to draw overlays, false otherwise.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    /**
     * Creates an intent to request overlay permission.
     *
     * @param context The application context.
     * @return An intent that can be used to request overlay permission.
     */
    fun createOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
    
    /**
     * Logs the current overlay permission status.
     *
     * @param context The application context.
     */
    fun logOverlayPermissionStatus(context: Context) {
        val hasPermission = hasOverlayPermission(context)
        Log.d(TAG, "Overlay permission status: $hasPermission")
    }
} 