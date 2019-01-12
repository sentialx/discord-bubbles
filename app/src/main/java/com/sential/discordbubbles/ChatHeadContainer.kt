package com.sential.discordbubbles

import android.animation.ValueAnimator
import android.animation.PropertyValuesHolder

class ChatHeadContainer() {
    val chatHead = ChatHead(OverlayService.instance)
    val chatHeadLayout = ChatHeadLayout(this)

    var isTop: Boolean = false
    var isSelected = false

    var server: String = ""
}