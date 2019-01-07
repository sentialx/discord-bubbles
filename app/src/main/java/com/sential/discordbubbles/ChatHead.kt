package com.sential.discordbubbles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView

class ChatHead(container: ChatHeadContainer) {
    var params: WindowManager.LayoutParams

    var view = ImageView(OverlayService.instance)

    var x: Int
        get() = params.x
        set(value) {
            params.x = value
            OverlayService.instance.windowManager.updateViewLayout(view, params)
        }

    var y: Int
        get() = params.y
        set(value) {
            params.y = value
            OverlayService.instance.windowManager.updateViewLayout(view, params)
        }

    init {
        view.adjustViewBounds = true
        view.scaleType = ImageView.ScaleType.CENTER_CROP

        val bitmap = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.test), 10000f);

        view.setImageBitmap(ImageHelper.addShadow(bitmap));

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManagerHelper.getLayoutFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = (Gravity.TOP or Gravity.START) or Gravity.DISPLAY_CLIP_VERTICAL
        params.x = 0
        params.y = 0
        params.width = ChatHeadsArrangement.CHAT_HEAD_SIZE
        params.height = ChatHeadsArrangement.CHAT_HEAD_SIZE

        OverlayService.instance.windowManager.addView(view, params)
    }

    /*fun hide() {
        params.x = lastX
        params.y = lastY
        OverlayService.instance.windowManager.updateViewLayout(view, params)
        toggled = false
    }*/
}