package com.sential.discordbubbles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import java.lang.Math.*

class Bubble : ImageView, View.OnTouchListener {
    private val OUT_OF_SCREEN_X: Float = OverlayService.dpToPx(16f).toFloat()
    private val DRAG_TOLERANCE: Float = 15f

    lateinit var params: WindowManager.LayoutParams

    lateinit var view: LinearLayout
    lateinit var imageView: ImageView

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f
    private var moving = false

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var toggled = false

    private var isOnRight = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return ((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        imageView = ImageView(context)
        view = LinearLayout(context)

        view.clipChildren = false

        imageView.x = -OUT_OF_SCREEN_X
        imageView.setOnTouchListener(this)
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        val bitmap = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(resources, R.drawable.test), 10000);

        imageView.setImageBitmap(ImageHelper.addShadow(bitmap));

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            OverlayService.getLayoutFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val size = OverlayService.dpToPx(78f)

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100
        params.width = size
        params.height = size

        view.addView(imageView)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    v!!.performClick()

                    if (!toggled) {
                        lastX = params.x
                        lastY = params.y

                        params.x = 0
                        params.y = 0

                        OverlayService.instance.overlayLayout.show()
                        OverlayService.instance.windowManager.updateViewLayout(view, params)

                        toggled = true
                    } else {
                        OverlayService.instance.hide()
                    }
                }

                moving = false
                val metrics = OverlayService.getScreenSize()

                if (toggled) {
                    params.x = metrics.widthPixels - view.width
                    params.y = 0
                    imageView.x = 0f
                } else {
                    if (params.x >= metrics.widthPixels / 2) {
                        params.x = metrics.widthPixels - view.width
                        imageView.x = OUT_OF_SCREEN_X
                        isOnRight = true
                    } else if (params.x < metrics.widthPixels / 2) {
                        params.x = 0
                        imageView.x = -OUT_OF_SCREEN_X
                        isOnRight = false
                    }
                }


                OverlayService.instance.windowManager.updateViewLayout(view, params)
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > DRAG_TOLERANCE * DRAG_TOLERANCE) {
                    moving = true
                }

                if (toggled) {
                    params.x = (initialX + (event.rawX - initialTouchX)).toInt()
                } else {
                   if (isOnRight && min(0f, event.rawX - initialTouchX) > -OUT_OF_SCREEN_X) {
                       imageView.x = OUT_OF_SCREEN_X + min(0f, event.rawX - initialTouchX)
                   } else if (!isOnRight && event.rawX - initialTouchX < OUT_OF_SCREEN_X) {
                       imageView.x = -OUT_OF_SCREEN_X + max(0f, event.rawX - initialTouchX)
                   } else {
                       imageView.x = 0f
                       params.x = (initialX + (event.rawX - initialTouchX)).toInt() + if (isOnRight) OUT_OF_SCREEN_X.toInt() else -OUT_OF_SCREEN_X.toInt()
                   }
                }

                params.y = (initialY + (event.rawY - initialTouchY)).toInt()

                OverlayService.instance.windowManager.updateViewLayout(view, params)
            }
        }

        return true
    }


    fun hide() {
        imageView.x = if (isOnRight) OUT_OF_SCREEN_X else -OUT_OF_SCREEN_X
        params.x = lastX
        params.y = lastY
        OverlayService.instance.windowManager.updateViewLayout(view, params)
        toggled = false
    }
}