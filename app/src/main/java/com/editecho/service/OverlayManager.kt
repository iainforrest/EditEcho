package com.editecho.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isOverlayPermissionGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun startOverlay() {
        if (!isOverlayPermissionGranted()) {
            requestOverlayPermission()
            return
        }

        val intent = Intent(context, OverlayService::class.java)
        context.startService(intent)
    }

    fun stopOverlay() {
        val intent = Intent(context, OverlayService::class.java)
        context.stopService(intent)
    }
} 