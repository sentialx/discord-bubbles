package com.sential.discordbubbles

import android.animation.ValueAnimator
import android.animation.PropertyValuesHolder

class ChatHeadContainer() {
    val chatHead = ChatHead(OverlayService.instance)
    val chatHeadLayout = ChatHeadLayout(this)

    var isTop: Boolean = false
    var isSelected = false

    var server: String = ""

    fun animate(endX: Float, endY: Float, duration: Int = 0) {
        val pvhX = PropertyValuesHolder.ofFloat("x", chatHead.x, endX)
        val pvhY = PropertyValuesHolder.ofFloat("y", chatHead.y, endY)

        val translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY)

        translator.addUpdateListener { valueAnimator ->
            chatHead.x = valueAnimator.getAnimatedValue("x") as Float
            chatHead.y = valueAnimator.getAnimatedValue("y") as Float
        }

        translator.duration = duration.toLong()
        translator.start()
    }
}