package com.sential.discordbubbles

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.widget.ImageView
import android.graphics.*


class OverlayService : Service(),View.OnTouchListener,View.OnClickListener {
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var windowManager: WindowManager
    private lateinit var overlayButton: ImageView

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f
    private var moving = false

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayButton = ImageView(this)
        overlayButton.setOnTouchListener(this)
        overlayButton.setOnClickListener(this)
        overlayButton.adjustViewBounds = true
        overlayButton.scaleType = ImageView.ScaleType.CENTER_CROP

        var bitmap = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.test), 10000);

        overlayButton.setImageBitmap(ImageHelper.addShadow(bitmap));

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        params.x = 0
        params.y = 100
        params.width = 190
        params.height = 190

        windowManager.addView(overlayButton, params)
    }

    override fun onDestroy() {
        super.onDestroy()

        windowManager.removeView(overlayButton)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    view!!.performClick()
                }

                moving = false

                val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay

                val size = Point()
                display.getSize(size)

                if (params.x >= size.x / 2) {
                    params.x = size.x - overlayButton.width
                } else if (params.x < size.x / 2) {
                    params.x = 0
                }

                windowManager.updateViewLayout(overlayButton, params)
            }
            MotionEvent.ACTION_MOVE -> {
                moving = true
                params.x = (initialX + (event.rawX - initialTouchX)).toInt()
                params.y = (initialY + (event.rawY - initialTouchY)).toInt()
                windowManager.updateViewLayout(overlayButton, params)
            }
        }

        return true
    }

    override fun onClick(view: View?) {
        Toast.makeText(this, "button touched", Toast.LENGTH_SHORT).show()
    }

}