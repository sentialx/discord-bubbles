package com.sential.discordbubbles

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import kotlin.math.pow

class ChatHeadsArrangement(context: Context) : View.OnTouchListener, FrameLayout(context) {
    companion object {
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = WindowManagerHelper.dpToPx(12f)
        val CHAT_HEAD_SIZE: Int = WindowManagerHelper.dpToPx(64f)
        val CHAT_HEAD_PADDING: Int = WindowManagerHelper.dpToPx(6f)
        val CHAT_HEAD_EXPANDED_PADDING: Int = WindowManagerHelper.dpToPx(2f)

        const val CHAT_HEAD_DRAG_TOLERANCE: Float = 20f
    }

    var chatHeads = ArrayList<ChatHeadContainer>()

    private var initialX = 0.0f
    private var initialY = 0.0f

    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f

    private var lastX = 0.0f
    private var lastY = 0.0f

    private var moving = false
    private var toggled = false

    private var topChatHead: ChatHeadContainer? = null

    private var isOnRight = false

    var motionTracker = FrameLayout(context)

    var motionTrackerParams = WindowManager.LayoutParams(
        CHAT_HEAD_SIZE,
        CHAT_HEAD_SIZE,
        WindowManagerHelper.getLayoutFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    )

    var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManagerHelper.getLayoutFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    )

    init {
        params.gravity = Gravity.START or Gravity.TOP
        motionTrackerParams.gravity = Gravity.START or Gravity.TOP
        params.dimAmount = 0.5f

        OverlayService.instance.windowManager.addView(motionTracker, motionTrackerParams)
        OverlayService.instance.windowManager.addView(this, params)

        motionTracker.setOnTouchListener(this)

        this.setOnTouchListener{ v, event ->
            v!!.performClick()

            if (v == this) {
                collapseChatHeads()
            }

            return@setOnTouchListener true
        }
    }

    fun setTop(chatHead: ChatHeadContainer) {
        topChatHead?.isTop = false
        chatHead.isTop = true

        topChatHead = chatHead

        val index = chatHeads.indexOf(chatHead)
        chatHeads.removeAt(index)
        chatHeads.add(0, chatHead)

        chatHead.chatHead.bringToFront()
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

        chatHeads.forEachIndexed { index, element ->
            element.chatHead.z = chatHeads.size - index.toFloat()
            element.chatHead.x = -CHAT_HEAD_OUT_OF_SCREEN_X - CHAT_HEAD_PADDING * index.toFloat()
        }

        return chatHeadContainer
    }

    fun collapseChatHeads() {
        chatHeads.forEachIndexed { index, element ->
            element.isSelected = false
            element.animate(lastX - (CHAT_HEAD_PADDING * index * if (isOnRight) -1 else 1), lastY, 150)
            element.chatHead.setOnTouchListener(null)

            motionTrackerParams.flags = motionTrackerParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

            params.flags = (params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            OverlayService.instance.windowManager.updateViewLayout(this, params)

            //element.chatHeadLayout.hide()
        }

        toggled = false
    }

    private fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return ((x1 - x2).pow(2) + (y1-y2).pow(2))
    }

    private val onChatHeadTouch: View.OnTouchListener = View.OnTouchListener { view, event ->
        val currentChatHead = chatHeads.find { it.chatHead == view }!!

        val metrics = WindowManagerHelper.getScreenSize()

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = currentChatHead!!.chatHead.x
                initialY = currentChatHead!!.chatHead.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                currentChatHead.chatHead.scaleX = 0.95f
                currentChatHead.chatHead.scaleY = 0.95f
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    if (currentChatHead.isSelected) {
                        collapseChatHeads()
                    } else {
                        val selectedChatHead = chatHeads.find { it.isSelected }!!
                        selectedChatHead.isSelected = false
                        //selectedChatHead.chatHeadLayout.hide()

                        currentChatHead.isSelected = true
                        //currentChatHead.chatHeadLayout.show()
                    }
                } else {
                    currentChatHead.animate(metrics.widthPixels - currentChatHead.chatHead.width - (chatHeads.indexOf(currentChatHead) * (currentChatHead.chatHead.width + CHAT_HEAD_EXPANDED_PADDING)).toFloat(), 0f, 200)
                }

                currentChatHead.chatHead.scaleX = 1f
                currentChatHead.chatHead.scaleY = 1f

                moving = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                }

                if (moving) {
                    currentChatHead.animate(initialX + (event.rawX - initialTouchX), initialY + (event.rawY - initialTouchY))
                }
            }
        }

        return@OnTouchListener true
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val reversedChatHeads = chatHeads.asReversed()

        val metrics = WindowManagerHelper.getScreenSize()

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = topChatHead!!.chatHead.x
                initialY = topChatHead!!.chatHead.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                topChatHead?.chatHead?.scaleX = 0.95f
                topChatHead?.chatHead?.scaleY = 0.95f
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    if (!toggled) {
                        toggled = true

                        lastX = topChatHead?.chatHead?.x!!
                        lastY = topChatHead?.chatHead?.y!!

                        chatHeads.forEachIndexed { index, it ->
                            it.animate(metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width + CHAT_HEAD_EXPANDED_PADDING)).toFloat(), 0f, 150)
                            it.chatHead.setOnTouchListener(onChatHeadTouch)
                        }

                        //topChatHead?.chatHeadLayout?.show()

                        topChatHead?.isSelected = true

                        motionTrackerParams.flags = motionTrackerParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

                        params.flags = (params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        OverlayService.instance.windowManager.updateViewLayout(this, params)
                    }
                } else if (toggled) {
                    chatHeads.forEachIndexed { index, it ->
                        it.animate(metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width)).toFloat(), 0f, 200)
                    }
                } else if (topChatHead?.chatHead?.x!! >= metrics.widthPixels / 2) {
                    reversedChatHeads.forEachIndexed { index, element ->
                        element.animate(metrics.widthPixels - topChatHead?.chatHead?.width!! + CHAT_HEAD_OUT_OF_SCREEN_X + CHAT_HEAD_PADDING * (chatHeads.size - 1 - index).toFloat(), element.chatHead.y, 200)
                    }
                    isOnRight = true
                } else if (topChatHead?.chatHead?.x!! < metrics.widthPixels / 2) {
                    reversedChatHeads.forEachIndexed { index, element ->
                        element.animate(-CHAT_HEAD_OUT_OF_SCREEN_X - CHAT_HEAD_PADDING * (chatHeads.size - 1 - index).toFloat(), element.chatHead.y, 200)
                    }
                    isOnRight = false
                }

                if (moving) {
                    motionTrackerParams.x = if (isOnRight) metrics.widthPixels - topChatHead?.chatHead?.width!! else 0
                    motionTrackerParams.y = topChatHead!!.chatHead.y.toInt()

                    OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
                }

                topChatHead?.chatHead?.scaleX = 1f
                topChatHead?.chatHead?.scaleY = 1f

                moving = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                }

                if (moving) {
                    reversedChatHeads.forEachIndexed { index, element ->
                        element.animate((initialX + (event.rawX - initialTouchX)) - (CHAT_HEAD_PADDING * (chatHeads.size - 1 - index)) * if (isOnRight) -1 else 1, (initialY + (event.rawY - initialTouchY)))
                    }
                }
            }
        }

        return true
    }
}