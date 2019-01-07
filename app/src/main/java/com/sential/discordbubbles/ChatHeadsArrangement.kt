package com.sential.discordbubbles

import android.view.MotionEvent
import android.view.View
import kotlin.math.pow

class ChatHeadsArrangement : View.OnTouchListener {
    companion object {
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = WindowManagerHelper.dpToPx(16f)
        val CHAT_HEAD_SIZE: Int = WindowManagerHelper.dpToPx(78f)
        const val CHAT_HEAD_DRAG_TOLERANCE: Float = 20f
        const val CHAT_HEAD_PADDING: Int = 16
    }

    var chatHeads = ArrayList<ChatHeadContainer>()

    private var initialX: Int = 0
    private var initialY: Int = 0

    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f

    private var lastX: Int = 0
    private var lastY: Int = 0

    private var moving = false
    private var toggled = false

    private var topChatHead: ChatHeadContainer? = null

    private var isOnRight = false

    fun setTop(chatHead: ChatHeadContainer) {
        topChatHead?.isTop = false
        chatHead.isTop = true

        topChatHead = chatHead

        val index = chatHeads.indexOf(chatHead)
        chatHeads.removeAt(index)
        chatHeads.add(0, chatHead)
    }

    fun addChatHead(isTop: Boolean = false) {
        if (isTop) {
            chatHeads.forEach {
                if (it.isTop) {
                    it.isTop = false
                }
            }
        }

        val chatHeadContainer = ChatHeadContainer()
        chatHeads.add(chatHeadContainer)

        if (isTop) setTop(chatHeadContainer)

        chatHeads.asReversed().forEachIndexed { index, element ->
            element.chatHead.x = -CHAT_HEAD_OUT_OF_SCREEN_X - CHAT_HEAD_PADDING * (chatHeads.size - 1 - index)
            OverlayService.instance.windowManager.removeView(element.chatHead.view)
            OverlayService.instance.windowManager.addView(element.chatHead.view, element.chatHead.params)
        }
    }

    private fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return ((x1 - x2).pow(2) + (y1-y2).pow(2))
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
       when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = topChatHead?.chatHead?.x!!
                initialY = topChatHead?.chatHead?.y!!
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                val metrics = WindowManagerHelper.getScreenSize()

                if (!moving) {
                    v!!.performClick()

                    if (!toggled) {
                        toggled = true

                        lastX = topChatHead?.chatHead?.x!!
                        lastY = topChatHead?.chatHead?.y!!

                        topChatHead?.chatHead?.x = metrics.widthPixels - topChatHead?.chatHead?.view?.width!!
                        topChatHead?.chatHead?.y = 0

                        chatHeads.forEachIndexed { index, it ->
                            it.chatHead.x = metrics.widthPixels - it.chatHead.view.width - (index * (it.chatHead.view.width - WindowManagerHelper.dpToPx(8f)))
                            it.chatHead.y = 0
                        }

                        topChatHead?.chatHeadLayout?.show()
                    } else {
                        chatHeads.forEachIndexed { index, element ->
                            element.chatHead.x = lastX - (CHAT_HEAD_PADDING * index * if (isOnRight) -1 else 1)
                            element.chatHead.y = lastY
                            element.chatHeadLayout.hide()
                        }

                        toggled = false
                    }

                    return true
                }

                moving = false

                if (topChatHead?.chatHead?.x!! >= metrics.widthPixels / 2) {
                    chatHeads.asReversed().forEachIndexed { index, element ->
                        element.chatHead.x = metrics.widthPixels - topChatHead?.chatHead?.view?.width!! + CHAT_HEAD_OUT_OF_SCREEN_X + CHAT_HEAD_PADDING * (chatHeads.size - 1 - index)
                    }
                    isOnRight = true
                } else if (topChatHead?.chatHead?.x!! < metrics.widthPixels / 2) {
                    chatHeads.asReversed().forEachIndexed { index, element ->
                        element.chatHead.x = -CHAT_HEAD_OUT_OF_SCREEN_X - CHAT_HEAD_PADDING * (chatHeads.size - 1 - index)
                    }
                    isOnRight = false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                }

                if (moving) {
                    chatHeads.asReversed().forEachIndexed { index, element ->
                        element.chatHead.x = (initialX + (event.rawX - initialTouchX)).toInt() - (CHAT_HEAD_PADDING * (chatHeads.size - 1 - index)) * if (isOnRight) -1 else 1
                        element.chatHead.y = (initialY + (event.rawY - initialTouchY)).toInt()
                    }
                }
            }
        }

        return true
    }
}