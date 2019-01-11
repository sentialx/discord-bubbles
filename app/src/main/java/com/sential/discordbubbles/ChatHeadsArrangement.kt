package com.sential.discordbubbles

import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlin.math.pow
import android.view.animation.Animation
import android.view.animation.ScaleAnimation



class ChatHeadsArrangement(context: Context) : View.OnTouchListener, FrameLayout(context) {
    companion object {
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = WindowManagerHelper.dpToPx(12f)
        val CHAT_HEAD_SIZE: Int = WindowManagerHelper.dpToPx(64f)
        val CHAT_HEAD_PADDING: Int = WindowManagerHelper.dpToPx(6f)
        val CHAT_HEAD_EXPANDED_PADDING: Int = WindowManagerHelper.dpToPx(2f)
        val CHAT_HEAD_EXPANDED_MARGIN_TOP: Float = WindowManagerHelper.dpToPx(4f).toFloat()

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

    var content = LayoutInflater.from(OverlayService.instance).inflate(R.layout.chat_head_content, null)

    var motionTracker = LinearLayout(context)

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
        params.dimAmount = 0.5f

        motionTrackerParams.gravity = Gravity.START or Gravity.TOP

        OverlayService.instance.windowManager.addView(motionTracker, motionTrackerParams)
        OverlayService.instance.windowManager.addView(this, params)
        this.addView(content)

        content.visibility = View.GONE

        motionTracker.setOnTouchListener(this)

        this.setOnTouchListener{ v, event ->
            v.performClick()

            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (v == this) {
                        collapseChatHeads()
                    }
                }

            }

            return@setOnTouchListener false
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
        }

        motionTrackerParams.flags = motionTrackerParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

        params.flags = (params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        OverlayService.instance.windowManager.updateViewLayout(this, params)

        content.visibility = View.GONE

        content.visibility = View.VISIBLE
        val anim = ScaleAnimation(
            1f,
            0f,
            1f,
            0f,
            Animation.RELATIVE_TO_SELF,
            if (isOnRight) 0.95f else 0.05f,
            Animation.RELATIVE_TO_SELF,
            lastY / this.height + 0.08f
        )
        anim.duration = 100
        anim.fillAfter = true
        content.startAnimation(anim)

        android.os.Handler().postDelayed(
            {
                content.visibility = View.GONE
            }, 100
        )



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
                    currentChatHead.animate(metrics.widthPixels - currentChatHead.chatHead.width - (chatHeads.indexOf(currentChatHead) * (currentChatHead.chatHead.width + CHAT_HEAD_EXPANDED_PADDING)).toFloat(), CHAT_HEAD_EXPANDED_MARGIN_TOP, 200)
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
                            it.animate(metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width + CHAT_HEAD_EXPANDED_PADDING)).toFloat(), CHAT_HEAD_EXPANDED_MARGIN_TOP, 150)
                            it.chatHead.setOnTouchListener(onChatHeadTouch)
                        }

                        android.os.Handler().postDelayed(
                            {
                                content.visibility = View.VISIBLE
                                val anim = ScaleAnimation(
                                    0f,
                                    1f,
                                    0f,
                                    1f,
                                    Animation.RELATIVE_TO_SELF,
                                    0.95f,
                                    Animation.RELATIVE_TO_SELF,
                                    0.08f
                                )
                                anim.duration = 100
                                anim.fillAfter = true
                                content.startAnimation(anim)
                            }, 220
                        )

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