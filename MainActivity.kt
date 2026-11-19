package com.example.clumsy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private val prefs by lazy { getSharedPreferences("clumsy_access", MODE_PRIVATE) }
    private val key = "@Gem4ktop"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen()
    }

    private fun showScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(24))
        }
        layout.addView(TextView(this).apply {
            text = "CLUMSY MOBILE"
            textSize = 26f
        })
        if (!prefs.getBoolean("activated", false)) {
            layout.addView(TextView(this).apply {
                text = "Enter access key to continue"
                textSize = 16f
                setPadding(0, dp(16), 0, dp(12))
            })
            val input = EditText(this).apply {
                hint = "Access key"
                singleLine = true
            }
            layout.addView(input)
            layout.addView(Button(this).apply {
                text = "Activate"
                setOnClickListener {
                    if (input.text.toString().trim() == key) {
                        prefs.edit().putBoolean("activated", true).apply()
                        Toast.makeText(this@MainActivity, "Key accepted", Toast.LENGTH_SHORT).show()
                        showScreen()
                    } else {
                        input.error = "Invalid key"
                    }
                }
            })
        } else {
            layout.addView(TextView(this).apply {
                text = "Activated ✓\nFloating demo button. This overlay does not alter Roblox traffic or freeze gameplay."
                textSize = 16f
                setPadding(0, dp(16), 0, dp(16))
            })
            layout.addView(Button(this).apply {
                text = "Allow display over other apps"
                setOnClickListener {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                }
            })
            layout.addView(Button(this).apply {
                text = "Start floating button"
                setOnClickListener {
                    if (Settings.canDrawOverlays(this@MainActivity)) {
                        startService(Intent(this@MainActivity, OverlayService::class.java))
                    } else {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")))
                    }
                }
            })
            layout.addView(Button(this).apply {
                text = "Stop overlay"
                setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) }
            })
            layout.addView(Button(this).apply {
                text = "Reset activation"
                setOnClickListener {
                    prefs.edit().putBoolean("activated", false).apply()
                    stopService(Intent(this@MainActivity, OverlayService::class.java))
                    showScreen()
                }
            })
        }
        setContentView(layout)
    }
}
