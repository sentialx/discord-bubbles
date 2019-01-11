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
        0,
        PixelFormat.TRANSLUCENT
    )

    val paint = Paint()

    val bitmap = ImageHelper.addShadow(ImageHelper.getCircularBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.test)))

    init {
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = ChatHeadsArrangement.CHAT_HEAD_SIZE + 15
        params.height = ChatHeadsArrangement.CHAT_HEAD_SIZE + 30

        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        OverlayService.instance.chatHeadsArrangement.addView(this, params)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
    }
}