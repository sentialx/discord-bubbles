package com.sential.discordbubbles

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.*
import com.facebook.rebound.*


class ChatHead(context: Context): View(context), SpringListener {
    override fun onSpringEndStateChange(spring: Spring?) {

    }

    override fun onSpringAtRest(spring: Spring?) {

    }

    override fun onSpringActivate(spring: Spring?) {

    }

    var params: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManagerHelper.getLayoutFlag(),
        0,
        PixelFormat.TRANSLUCENT
    )

    var springSystem = SpringSystem.create()

    var springX = springSystem.createSpring()
    var springY = springSystem.createSpring()

    val paint = Paint()

    val bitmap = ImageHelper.addShadow(ImageHelper.getCircularBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.test)))

    init {
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = ChatHeadsArrangement.CHAT_HEAD_SIZE + 15
        params.height = ChatHeadsArrangement.CHAT_HEAD_SIZE + 30

        springX.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                x = spring.currentValue.toFloat()
            }
        })
        springX.springConfig = SpringConfigs.NOT_DRAGGING
        springX.addListener(this)

        springY.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                y = spring.currentValue.toFloat()
            }
        })
        springY.springConfig = SpringConfigs.NOT_DRAGGING
        springY.addListener(this)

        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        OverlayService.instance.chatHeadsArrangement.addView(this, params)
    }

    override fun onSpringUpdate(spring: Spring) {
        if (spring !== this.springX && spring !== this.springY) return
        val totalVelocity = Math.hypot(springX.velocity, springY.velocity).toInt()

        OverlayService.instance.chatHeadsArrangement.onSpringUpdate(this, spring, totalVelocity)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmap, 0f, 0f, paint)
    }
}