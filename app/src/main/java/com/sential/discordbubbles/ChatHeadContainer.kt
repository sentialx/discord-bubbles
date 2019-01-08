package com.sential.discordbubbles

import android.animation.ValueAnimator
import android.animation.PropertyValuesHolder
import android.view.View

class ChatHeadContainer() {
    val chatHead = ChatHead(OverlayService.instance)
    val chatHeadLayout = ChatHeadLayout(this)

    var isTop: Boolean = false
        set(value) {
            field = value

            if (value) {
                chatHead.setOnTouchListener(OverlayService.instance.chatHeadsArrangement)
            } else {
                chatHead.setOnTouchListener(null)
            }
        }

    var isSelected = false

    var server: String = ""

    fun animate(endX: Int, endY: Int, duration: Int = 0) {
        val pvhX = PropertyValuesHolder.ofInt("x", chatHead.x, endX)
        val pvhY = PropertyValuesHolder.ofInt("y", chatHead.y, endY)

        val translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY)

        translator.addUpdateListener { valueAnimator ->
            val layoutParams = chatHead.params
            layoutParams.x = valueAnimator.getAnimatedValue("x") as Int
            layoutParams.y = valueAnimator.getAnimatedValue("y") as Int
            OverlayService.instance.windowManager.updateViewLayout(chatHead, layoutParams)
        }

        translator.duration = duration.toLong()
        translator.start()
    }
}