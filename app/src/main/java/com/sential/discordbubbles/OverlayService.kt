package com.sential.discordbubbles

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import android.widget.ImageView
import android.graphics.*
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.view.Window.ID_ANDROID_CONTENT
import java.lang.Math.abs
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.support.v4.content.ContextCompat.getSystemService
import android.view.inputmethod.InputMethodManager


class OverlayService : Service(),View.OnTouchListener,View.OnClickListener {
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var layoutParams: WindowManager.LayoutParams

    private lateinit var windowManager: WindowManager
    private lateinit var bubble: ImageView
    private lateinit var floatingView: View
    private lateinit var darkBackground: LinearLayout
    private lateinit var editText: EditText
    private lateinit var contentLayout: LinearLayout

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f
    private var moving = false

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var toggled = false

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.chat, null);
        floatingView.visibility = View.GONE;

        darkBackground = floatingView.findViewById(R.id.darkBg)
        darkBackground.setOnClickListener {
            hide()
        }

        contentLayout = floatingView.findViewById(R.id.contentLayout)

        editText = floatingView.findViewById(R.id.editText)

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                editText.requestFocus()
                val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }


        bubble = ImageView(this)
        bubble.setOnTouchListener(this)
        bubble.setOnClickListener(this)
        bubble.adjustViewBounds = true
        bubble.scaleType = ImageView.ScaleType.CENTER_CROP

        var bitmap = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.test), 10000);

        bubble.setImageBitmap(ImageHelper.addShadow(bitmap));

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        bubbleParams.gravity = Gravity.TOP or Gravity.START
        bubbleParams.x = 0
        bubbleParams.y = 100
        bubbleParams.width = 190
        bubbleParams.height = 190

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val size = Point()
        display.getSize(size)


        layoutParams.width = size.x
        layoutParams.height = size.y - 63
        layoutParams.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(floatingView, layoutParams)
        windowManager.addView(bubble, bubbleParams)
    }

    override fun onDestroy() {
        super.onDestroy()

        windowManager.removeView(bubble)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = bubbleParams.x
                initialY = bubbleParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    view!!.performClick()

                    if (!toggled) {
                        lastX = bubbleParams.x
                        lastY = bubbleParams.y

                        bubbleParams.x = 0
                        bubbleParams.y = 0

                        floatingView.visibility = View.VISIBLE;

                        windowManager.updateViewLayout(bubble, bubbleParams)
                        toggled = true
                    } else {
                        hide()
                    }
                }

                moving = false

                val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay

                val size = Point()
                display.getSize(size)

                if (bubbleParams.x >= size.x / 2) {
                    bubbleParams.x = size.x - bubble.width
                } else if (bubbleParams.x < size.x / 2) {
                    bubbleParams.x = 0
                }

                windowManager.updateViewLayout(bubble, bubbleParams)
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.rawX - initialTouchX) > 15 || abs(event.rawY - initialTouchY) > 15) {
                    moving = true
                }
                bubbleParams.x = (initialX + (event.rawX - initialTouchX)).toInt()
                bubbleParams.y = (initialY + (event.rawY - initialTouchY)).toInt()
                windowManager.updateViewLayout(bubble, bubbleParams)
            }
        }

        return true
    }

    override fun onClick(view: View?) {

    }

    fun hide() {
        floatingView.visibility = View.GONE;
        bubbleParams.x = lastX
        bubbleParams.y = lastY
        windowManager.updateViewLayout(bubble, bubbleParams)
        toggled = false
    }
}