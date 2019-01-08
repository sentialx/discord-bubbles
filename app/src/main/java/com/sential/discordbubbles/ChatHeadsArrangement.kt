package com.sential.discordbubbles

import android.view.MotionEvent
import android.view.View
import kotlin.math.pow

class ChatHeadsArrangement : View.OnTouchListener {
    companion object {
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = WindowManagerHelper.dpToPx(16f)
        val CHAT_HEAD_SIZE: Int = WindowManagerHelper.dpToPx(64f)
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

        chatHeads.asReversed().forEach {
            OverlayService.instance.windowManager.removeView(it.chatHead)
            OverlayService.instance.windowManager.addView(it.chatHead, it.chatHead.params)
        }
    }

    fun addChatHead(isTop: Boolean = false): ChatHeadContainer {
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
            OverlayService.instance.windowManager.removeView(element.chatHead)
            OverlayService.instance.windowManager.addView(element.chatHead, element.chatHead.params)
        }

        return chatHeadContainer
    }

    fun collapseChatHeads() {
        chatHeads.forEachIndexed { index, element ->
            element.isSelected = false
            element.animate(lastX - (CHAT_HEAD_PADDING * index * if (isOnRight) -1 else 1), lastY, 100)
            element.chatHeadLayout.hide()
        }

        toggled = false
    }

    private fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return ((x1 - x2).pow(2) + (y1-y2).pow(2))
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val reversedChatHeads = chatHeads.asReversed()

        val currentChatHead = chatHeads.find { it.chatHead == v }!!

       when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = currentChatHead.chatHead.x
                initialY = currentChatHead.chatHead.y
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

                        chatHeads.forEachIndexed { index, it ->
                            it.animate(metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width)), 0, 200)
                        }

                        topChatHead?.chatHeadLayout?.show()

                        topChatHead?.isSelected = true
                    } else if (currentChatHead.isSelected) {
                        collapseChatHeads()
                    } else {
                        val selectedChatHead = chatHeads.find { it.isSelected }!!
                        selectedChatHead.isSelected = false
                        selectedChatHead.chatHeadLayout.hide()

                        currentChatHead.isSelected = true
                        currentChatHead.chatHeadLayout.show()
                    }
                } else if (toggled) {
                    chatHeads.forEachIndexed { index, it ->
                        it.animate(metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width)), 0, 200)
                    }
                } else if (topChatHead?.chatHead?.x!! >= metrics.widthPixels / 2) {
                    reversedChatHeads.forEachIndexed { index, element ->
                        element.animate(metrics.widthPixels - topChatHead?.chatHead?.width!! + CHAT_HEAD_OUT_OF_SCREEN_X + CHAT_HEAD_PADDING * (chatHeads.size - 1 - index), element.chatHead.y, 200)
                    }
                    isOnRight = true
                } else if (topChatHead?.chatHead?.x!! < metrics.widthPixels / 2) {
                    reversedChatHeads.forEachIndexed { index, element ->
                        element.animate(-CHAT_HEAD_OUT_OF_SCREEN_X - CHAT_HEAD_PADDING * (chatHeads.size - 1 - index), element.chatHead.y, 200)
                    }
                    isOnRight = false
                }

                moving = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                }

                if (moving) {
                    if (toggled) {
                        currentChatHead.animate((initialX + (event.rawX - initialTouchX)).toInt(), (initialY + (event.rawY - initialTouchY)).toInt())
                    } else {
                        reversedChatHeads.forEachIndexed { index, element ->
                            element.animate((initialX + (event.rawX - initialTouchX)).toInt() - (CHAT_HEAD_PADDING * (chatHeads.size - 1 - index)) * if (isOnRight) -1 else 1, (initialY + (event.rawY - initialTouchY)).toInt())
                        }
                    }
                }
            }
        }

        chatHeads.forEach {
            OverlayService.instance.windowManager.updateViewLayout(it.chatHead, it.chatHead.params)
        }

        return true
    }
}