package com.sential.discordbubbles

class ChatHeadContainer() {
    val chatHead = ChatHead(this)
    val chatHeadLayout = ChatHeadLayout(this)

    var isTop: Boolean = false
        set(value) {
            field = value

            if (value) {
                chatHead.view.setOnTouchListener(OverlayService.instance.chatHeadsArrangement)
            } else {
                chatHead.view.setOnTouchListener(null)
            }
        }
}