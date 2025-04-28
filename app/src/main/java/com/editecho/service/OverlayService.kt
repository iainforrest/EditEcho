package com.editecho.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.editecho.R
import com.editecho.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var editText: EditText? = null
    private var sendButton: Button? = null
    private var closeButton: Button? = null

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }

    private fun setupOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        editText = overlayView?.findViewById(R.id.overlayEditText)
        sendButton = overlayView?.findViewById(R.id.sendButton)
        closeButton = overlayView?.findViewById(R.id.closeButton)

        sendButton?.setOnClickListener {
            val text = editText?.text?.toString() ?: ""
            if (text.isNotEmpty()) {
                // TODO: Handle sending text
                editText?.text?.clear()
            }
        }

        closeButton?.setOnClickListener {
            stopSelf()
        }

        windowManager?.addView(overlayView, params)
    }
} 