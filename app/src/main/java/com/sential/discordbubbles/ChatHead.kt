package com.sential.discordbubbles

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.*

class ChatHead(context: Context): View(context) {
    var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManagerHelper.getLayoutFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT
    )

    val paint = Paint()

    var x: Int
        get() = params.x
        set(value) {
            params.x = value
            OverlayService.instance.windowManager.updateViewLayout(this, params)
        }

    var y: Int
        get() = params.y
        set(value) {
            params.y = value
            OverlayService.instance.windowManager.updateViewLayout(this, params)
        }

    val bitmap = ImageHelper.addShadow(ImageHelper.getCircularBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.test)))

    init {
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = ChatHeadsArrangement.CHAT_HEAD_SIZE + 10
        params.height = ChatHeadsArrangement.CHAT_HEAD_SIZE + 10

        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        OverlayService.instance.windowManager.addView(this, params)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
    }
}