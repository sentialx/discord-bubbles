package com.sential.discordbubbles

import android.graphics.*
import android.view.*
import com.facebook.rebound.*

class Close(var chatHeads: ChatHeads): View(chatHeads.context) {
    private var params = WindowManager.LayoutParams(
        ChatHeads.CLOSE_SIZE + ChatHeads.CLOSE_ADDITIONAL_SIZE,
        ChatHeads.CLOSE_SIZE + ChatHeads.CLOSE_ADDITIONAL_SIZE,
        WindowManagerHelper.getLayoutFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    )

    var springSystem = SpringSystem.create()

    var springY = springSystem.createSpring()
    var springScale = springSystem.createSpring()

    val paint = Paint()

    private var bitmapBg = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.close_bg), ChatHeads.CLOSE_SIZE, ChatHeads.CLOSE_SIZE, false)!!
    private val bitmapClose = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.close), WindowManagerHelper.dpToPx(28f), WindowManagerHelper.dpToPx(28f), false)!!

    init {
        this.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

        val metrics = WindowManagerHelper.getScreenSize()

        visibility = View.INVISIBLE

        springY.endValue = metrics.heightPixels.toFloat() + height.toDouble()

        springY.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                y = spring.currentValue.toFloat()

                if (chatHeads.captured && chatHeads.wasMoving) {
                    chatHeads.topChatHead!!.springY.currentValue = spring.currentValue
                }
            }
        })

        springScale.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                bitmapBg =  Bitmap.createScaledBitmap(BitmapFactory.decodeResource(OverlayService.instance.resources, R.drawable.close_bg), (spring.currentValue + ChatHeads.CLOSE_SIZE).toInt(), (spring.currentValue + ChatHeads.CLOSE_SIZE).toInt(), false)
                invalidate()
            }
        })

        springScale.springConfig = SpringConfigs.CLOSE_SCALE
        springY.springConfig = SpringConfigs.NOT_DRAGGING

        params.gravity = Gravity.START or Gravity.TOP

        z = 100f

        chatHeads.addView(this, params)
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmapBg, width / 2 - bitmapBg.width.toFloat() / 2, height / 2 - bitmapBg.height.toFloat() / 2, paint)
        canvas?.drawBitmap(bitmapClose, width / 2 - bitmapClose.width.toFloat() / 2, height / 2 - bitmapClose.height.toFloat() / 2, paint)
    }
}