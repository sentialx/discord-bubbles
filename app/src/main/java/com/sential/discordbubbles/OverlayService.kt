package com.sential.discordbubbles

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.IBinder
import android.view.*
import android.util.DisplayMetrics

class OverlayService : Service() {
    lateinit var windowManager: WindowManager
    lateinit var bubble: Bubble
    lateinit var overlayLayout: OverlayLayout

    companion object {
        lateinit var instance: OverlayService

        fun getLayoutFlag(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }

        fun getScreenSize(): DisplayMetrics {
            return Resources.getSystem().displayMetrics
        }

        fun dpToPx(dp: Float): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        bubble = Bubble(this)
        overlayLayout = OverlayLayout(this)

        windowManager.addView(overlayLayout.view, overlayLayout.params)
        windowManager.addView(bubble.view, bubble.params)
    }

    override fun onDestroy() {
        super.onDestroy()

        windowManager.removeView(bubble)
        windowManager.removeView(overlayLayout)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun hide() {
        overlayLayout.hide()
        bubble.hide()
    }
}