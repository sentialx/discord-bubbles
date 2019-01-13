package com.sential.discordbubbles

import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlin.math.pow
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.VelocityTracker
import com.facebook.rebound.Spring
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.SpringChain
import kotlin.math.abs

class ChatHeadsArrangement(context: Context) : View.OnTouchListener, FrameLayout(context) {
    companion object {
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = WindowManagerHelper.dpToPx(10f)
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

    private var horizontalSpringChain: SpringChain = SpringChain.create()
    private var verticalSpringChain: SpringChain = SpringChain.create()

    var topChatHead: ChatHeadContainer? = null

    private var isOnRight = false

    private var velocityTracker: VelocityTracker? = null

    lateinit var currentSpring: Spring

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

        for (spring in horizontalSpringChain.allSprings) {
            spring.destroy()
        }

        for (spring in verticalSpringChain.allSprings) {
            spring.destroy()
        }

        horizontalSpringChain = SpringChain.create(0, 0, 200, 15)
        verticalSpringChain = SpringChain.create(0, 0, 200, 15)

        if (isTop) setTop(chatHeadContainer)

        chatHeads.forEachIndexed { index, element ->
            element.chatHead.z = (chatHeads.size - 1 - index).toFloat()

            if (element.isTop) {
                horizontalSpringChain.addSpring(object : SimpleSpringListener() {
                })
                verticalSpringChain.addSpring(object : SimpleSpringListener() {
                })

                element.chatHead.z = chatHeads.size.toFloat()
                horizontalSpringChain.setControlSpringIndex(index)
                verticalSpringChain.setControlSpringIndex(index)
            } else {
                horizontalSpringChain.addSpring(object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring?) {
                        element.chatHead.springX.currentValue = spring!!.currentValue + index * 20 * if (isOnRight) 1 else -1
                    }
                })
                verticalSpringChain.addSpring(object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring?) {
                        element.chatHead.springY.currentValue = spring!!.currentValue
                    }
                })
            }
        }

        return chatHeadContainer
    }

    fun collapseChatHeads() {
        chatHeads.forEachIndexed { index, element ->
            element.isSelected = false
            element.chatHead.springX.endValue = lastX - (CHAT_HEAD_PADDING * index * if (isOnRight) -1 else 1).toDouble()
            element.chatHead.springY.endValue = lastY.toDouble()
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
                initialX = currentChatHead.chatHead.x
                initialY = currentChatHead.chatHead.y
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
                    currentChatHead.chatHead.springX.endValue = metrics.widthPixels - currentChatHead.chatHead.width - (chatHeads.indexOf(currentChatHead) * (currentChatHead.chatHead.width + CHAT_HEAD_EXPANDED_PADDING)).toDouble()
                    currentChatHead.chatHead.springY.endValue = CHAT_HEAD_EXPANDED_MARGIN_TOP.toDouble()
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
                    currentChatHead.chatHead.springX.endValue = initialX + (event.rawX - initialTouchX).toDouble()
                    currentChatHead.chatHead.springY.endValue = initialY + (event.rawY - initialTouchY).toDouble()
                }
            }
        }

        return@OnTouchListener true
    }

    var initialVelocityX = 0.0
    var initialVelocityY = 0.0

    fun onSpringUpdate(chatHead: ChatHead, spring: Spring, totalVelocity: Int) {
        val metrics = WindowManagerHelper.getScreenSize()

        if (topChatHead != null && chatHead == topChatHead!!.chatHead) {
            if (spring == chatHead.springX) {
                horizontalSpringChain.controlSpring?.currentValue = spring.currentValue
            }

            if (spring == chatHead.springY) {
                verticalSpringChain.controlSpring?.currentValue = spring.currentValue
            }
        }

        if (abs(chatHead.springY.velocity) > 3000 && (chatHead.springX.currentValue > metrics.widthPixels - chatHead.width || chatHead.springX.currentValue < 0.0)) {
            chatHead.springY.velocity = 3000.0 * if (initialVelocityY < 0) -1 else 1
        }

        if ((chatHead.springX.currentValue < 0.0 && initialVelocityX < -3000 || chatHead.springX.currentValue > metrics.widthPixels - chatHead.width) && abs(initialVelocityY) < abs(initialVelocityX)) {
            chatHead.springY.velocity = 0.0
        }

        if (Math.abs(totalVelocity) < WindowManagerHelper.dpToPx(5000f) && !moving) {
            if (spring === chatHead.springX) {
                val xPosition = chatHead.springX.currentValue
                if (xPosition + chatHead.width > metrics.widthPixels && chatHead.springX.velocity > 0) {
                    val newPos = metrics.widthPixels - chatHead.width + CHAT_HEAD_OUT_OF_SCREEN_X
                    chatHead.springX.springConfig = SpringConfigs.NOT_DRAGGING
                    chatHead.springX.endValue = newPos.toDouble()
                    isOnRight = true
                } else if (xPosition < 0 && chatHead.springX.velocity < 0) {
                    chatHead.springX.springConfig = SpringConfigs.NOT_DRAGGING
                    chatHead.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                    isOnRight = false
                }
            } else if (spring === chatHead.springY) {
                val yPosition = chatHead.springY.currentValue
                if (yPosition + chatHead.height > metrics.heightPixels && chatHead.springY.velocity > 0) {
                    chatHead.springY.springConfig = SpringConfigs.NOT_DRAGGING
                    chatHead.springY.endValue = metrics.heightPixels - chatHead.height.toDouble() - WindowManagerHelper.dpToPx(25f)
                } else if (yPosition < 0 && chatHead.springY.velocity < 0) {
                    chatHead.springY.springConfig = SpringConfigs.NOT_DRAGGING
                    chatHead.springY.endValue = 0.0
                }
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val reversedChatHeads = chatHeads.asReversed()

        val metrics = WindowManagerHelper.getScreenSize()

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = topChatHead!!.chatHead.springX.currentValue.toFloat()
                initialY = topChatHead!!.chatHead.springY.currentValue.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                topChatHead?.chatHead?.scaleX = 0.95f
                topChatHead?.chatHead?.scaleY = 0.95f

                topChatHead?.chatHead!!.springX.springConfig = SpringConfigs.DRAGGING
                topChatHead?.chatHead!!.springY.springConfig = SpringConfigs.DRAGGING

                topChatHead?.chatHead!!.springX.setAtRest()
                topChatHead?.chatHead!!.springY.setAtRest()

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain()
                } else {
                    velocityTracker?.clear()
                }

                motionTrackerParams.x = topChatHead!!.chatHead.springX.currentValue.toInt()
                motionTrackerParams.y = topChatHead!!.chatHead.springY.currentValue.toInt()

                OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_UP -> {
                /*if (!moving) {
                    if (!toggled) {
                        toggled = true

                        lastX = topChatHead?.chatHead?.x!!
                        lastY = topChatHead?.chatHead?.y!!

                        chatHeads.forEachIndexed { index, it ->
                            it.chatHead.springX.endValue = metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width + CHAT_HEAD_EXPANDED_PADDING)).toDouble()
                            it.chatHead.springY.endValue = CHAT_HEAD_EXPANDED_MARGIN_TOP.toDouble()
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
                            }, 300
                        )

                        topChatHead?.isSelected = true

                        motionTrackerParams.flags = motionTrackerParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

                        params.flags = (params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        OverlayService.instance.windowManager.updateViewLayout(this, params)
                    }
                } else if (toggled) {
                    chatHeads.forEachIndexed { index, it ->
                        it.chatHead.springX.endValue = metrics.widthPixels - it.chatHead.width - (index * (it.chatHead.width)).toDouble()
                        it.chatHead.springY.endValue = 0.0
                    }
                }*/

                moving = false

                var xVelocity = velocityTracker!!.xVelocity.toDouble()
                var yVelocity = velocityTracker!!.yVelocity.toDouble()

                velocityTracker?.recycle()
                velocityTracker = null

                if (xVelocity < -3500) {
                    val newVelocity = ((-topChatHead!!.chatHead.springX.currentValue - 10 * CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                    if (xVelocity > newVelocity)
                        xVelocity = newVelocity
                } else if (xVelocity > 3500) {
                    val newVelocity = ((metrics.widthPixels - topChatHead!!.chatHead.springX.currentValue - topChatHead!!.chatHead.width + 10 * CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                    if (newVelocity > xVelocity)
                        xVelocity = newVelocity
                } else if (yVelocity > 20 || yVelocity < -20) {
                    topChatHead?.chatHead!!.springX.springConfig = SpringConfigs.NOT_DRAGGING

                    if (topChatHead?.chatHead?.x!! >= metrics.widthPixels / 2) {
                        topChatHead!!.chatHead.springX.endValue = metrics.widthPixels - topChatHead?.chatHead?.width!! + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                        isOnRight = true
                    } else {
                        topChatHead!!.chatHead.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()

                        isOnRight = false
                    }
                } else {
                    topChatHead?.chatHead!!.springX.springConfig = SpringConfigs.NOT_DRAGGING
                    topChatHead?.chatHead!!.springY.springConfig = SpringConfigs.NOT_DRAGGING

                    if (topChatHead?.chatHead?.x!! >= metrics.widthPixels / 2) {
                        topChatHead!!.chatHead.springX.endValue = metrics.widthPixels - topChatHead?.chatHead?.width!! +
                                CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                        topChatHead!!.chatHead.springY.endValue = topChatHead!!.chatHead.y.toDouble()

                        isOnRight = true
                    } else {
                        topChatHead!!.chatHead.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                        topChatHead!!.chatHead.springY.endValue = topChatHead!!.chatHead.y.toDouble()

                        isOnRight = false
                    }
                }

                initialVelocityX = xVelocity
                initialVelocityY = yVelocity

                topChatHead?.chatHead?.springX?.velocity = xVelocity
                topChatHead?.chatHead?.springY?.velocity = yVelocity

                topChatHead?.chatHead?.scaleX = 1f
                topChatHead?.chatHead?.scaleY = 1f

                android.os.Handler().postDelayed(
                    {
                        motionTrackerParams.x = topChatHead!!.chatHead.springX.currentValue.toInt()
                        motionTrackerParams.y = topChatHead!!.chatHead.springY.currentValue.toInt()

                        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
                    }, 400
                )
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                }

                velocityTracker?.addMovement(event)

                if (moving) {
                    topChatHead!!.chatHead.springX.currentValue = initialX + (event.rawX - initialTouchX).toDouble()
                    topChatHead!!.chatHead.springY.currentValue = initialY + (event.rawY - initialTouchY).toDouble()
                    velocityTracker?.computeCurrentVelocity(2000)
                }
            }
        }

        return true
    }
}