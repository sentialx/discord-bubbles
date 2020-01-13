package com.sential.discordbubbles

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.sential.discordbubbles.chatheads.OverlayService


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val canDraw: Boolean
        val intent: Intent?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            canDraw = Settings.canDrawOverlays(this)

            if (!canDraw) {
                startActivity(intent)
            }
        }

        val service = Intent(this, OverlayService::class.java)
        startService(service)
    }
}

