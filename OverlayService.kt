package com.example.clumsy

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.Vibrator
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var button: TextView? = null
    private var enabled = false
    private lateinit var params: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40; y = 220
        }

        button = TextView(this).apply {
            text = "CLUMSY\nOFF"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(28, 20, 28, 20)
            setBackgroundColor(0xCC303030.toInt())
            setOnClickListener {
                enabled = !enabled
                text = if (enabled) "CLUMSY\nON" else "CLUMSY\nOFF"
                setBackgroundColor(if (enabled) 0xCCB32626.toInt() else 0xCC303030.toInt())
                if (Build.VERSION.SDK_INT >= 26) {
                    (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                        android.os.VibrationEffect.createOneShot(45, 80)
                    )
                }
            }
            var downX = 0; var downY = 0; var startX = 0; var startY = 0
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.rawX.toInt(); downY = e.rawY.toInt()
                        startX = params.x; startY = params.y
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + e.rawX.toInt() - downX
                        params.y = startY + e.rawY.toInt() - downY
                        wm.updateViewLayout(v, params); true
                    }
                    else -> false
                }
            }
        }
        wm.addView(button, params)
    }
    override fun onDestroy() {
        button?.let { if (it.isAttachedToWindow) wm.removeView(it) }
        button = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
