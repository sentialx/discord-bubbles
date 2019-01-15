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
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
    private var wasMoving = false
    private var toggled = false
    private var motionTrackerUpdated = false

    private var horizontalSpringChain: SpringChain? = null
    private var verticalSpringChain: SpringChain? = null

    var topChatHead: ChatHeadContainer? = null

    private var isOnRight = false

    private var velocityTracker: VelocityTracker? = null

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

    fun destroySpringChains() {
        if (horizontalSpringChain != null) {
            for (spring in horizontalSpringChain!!.allSprings) {
                spring.destroy()
            }
        }

        if (verticalSpringChain != null) {
            for (spring in verticalSpringChain!!.allSprings) {
                spring.destroy()
            }
        }
    }

    fun resetSpringChains() {
        destroySpringChains()

        horizontalSpringChain = SpringChain.create(0, 0, 200, 15)
        verticalSpringChain = SpringChain.create(0, 0, 200, 15)

        val metrics = WindowManagerHelper.getScreenSize()

        chatHeads.forEachIndexed { index, element ->
            element.chatHead.z = index.toFloat()

            if (element.isTop) {
                horizontalSpringChain!!.addSpring(object : SimpleSpringListener() {
                })
                verticalSpringChain!!.addSpring(object : SimpleSpringListener() {
                })

                element.chatHead.z = chatHeads.size.toFloat()
                horizontalSpringChain!!.setControlSpringIndex(index)
                verticalSpringChain!!.setControlSpringIndex(index)
            } else {
                horizontalSpringChain!!.addSpring(object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring?) {
                        if (!toggled) {
                            element.chatHead.springX.currentValue = spring!!.currentValue + (chatHeads.size - 1 - index) * CHAT_HEAD_PADDING * if (isOnRight) 1 else -1
                        }
                    }
                })
                verticalSpringChain!!.addSpring(object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring?) {
                        if (!toggled) {
                            element.chatHead.springY.currentValue = spring!!.currentValue
                        }
                    }
                })
            }
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

        resetSpringChains()

        return chatHeadContainer
    }

    fun collapseChatHeads() {
        toggled = false

        topChatHead!!.chatHead.springX.endValue = lastX.toDouble()
        topChatHead!!.chatHead.springY.endValue = lastY.toDouble()

        chatHeads.forEachIndexed { index, element ->
            element.isSelected = false
            element.chatHead.setOnTouchListener(null)
        }

        motionTrackerParams.flags = motionTrackerParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

        params.flags = (params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        OverlayService.instance.windowManager.updateViewLayout(this, params)
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
                    currentChatHead.chatHead.springX.endValue = metrics.widthPixels - currentChatHead.chatHead.width - (chatHeads.size - 1 - chatHeads.indexOf(currentChatHead)) * (currentChatHead.chatHead.width + CHAT_HEAD_EXPANDED_PADDING).toDouble()
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
                    currentChatHead.chatHead.springX.currentValue = initialX + (event.rawX - initialTouchX).toDouble()
                    currentChatHead.chatHead.springY.currentValue = initialY + (event.rawY - initialTouchY).toDouble()
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
            if (horizontalSpringChain != null && spring == chatHead.springX) {
                horizontalSpringChain!!.controlSpring.currentValue = spring.currentValue
            }

            if (verticalSpringChain != null && spring == chatHead.springY) {
                verticalSpringChain!!.controlSpring.currentValue = spring.currentValue
            }
        }

        if (wasMoving) {
            if (abs(chatHead.springY.velocity) > 3000 && (chatHead.springX.currentValue > metrics.widthPixels - chatHead.width + CHAT_HEAD_OUT_OF_SCREEN_X / 2 || chatHead.springX.currentValue < -CHAT_HEAD_OUT_OF_SCREEN_X / 2) && abs(initialVelocityX) > 3000) {
                chatHead.springY.velocity = 3000.0 * if (initialVelocityY < 0) -1 else 1
            }

            if ((chatHead.springX.currentValue < -CHAT_HEAD_OUT_OF_SCREEN_X / 2 && initialVelocityX < -3000 || chatHead.springX.currentValue > metrics.widthPixels - chatHead.width  + CHAT_HEAD_OUT_OF_SCREEN_X / 2) && abs(initialVelocityY) < abs(initialVelocityX)) {
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
                        chatHead.springY.endValue = metrics.heightPixels - chatHead.height.toDouble() -
                                WindowManagerHelper.dpToPx(25f)
                    } else if (yPosition < 0 && chatHead.springY.velocity < 0) {
                        chatHead.springY.springConfig = SpringConfigs.NOT_DRAGGING
                        chatHead.springY.endValue = 0.0
                    }
                }

                motionTrackerParams.x = chatHead.springX.endValue.toInt()
                motionTrackerParams.y = chatHead.springY.endValue.toInt()

                OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val metrics = WindowManagerHelper.getScreenSize()

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = topChatHead!!.chatHead.springX.currentValue.toFloat()
                initialY = topChatHead!!.chatHead.springY.currentValue.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                wasMoving = false

                topChatHead?.chatHead?.scaleX = 0.95f
                topChatHead?.chatHead?.scaleY = 0.95f

                topChatHead?.chatHead!!.springX.springConfig = SpringConfigs.DRAGGING
                topChatHead?.chatHead!!.springY.springConfig = SpringConfigs.DRAGGING

                topChatHead?.chatHead!!.springX.setAtRest()
                topChatHead?.chatHead!!.springY.setAtRest()

                motionTrackerUpdated = false

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
                if (!moving) {
                    if (!toggled) {
                        toggled = true

                        lastX = topChatHead!!.chatHead.springX.currentValue.toFloat()
                        lastY = topChatHead!!.chatHead.springY.currentValue.toFloat()

                        chatHeads.forEachIndexed { index, it ->
                            it.chatHead.springX.springConfig = SpringConfigs.NOT_DRAGGING
                            it.chatHead.springY.springConfig = SpringConfigs.NOT_DRAGGING

                            it.chatHead.springY.endValue = CHAT_HEAD_EXPANDED_MARGIN_TOP.toDouble()
                            it.chatHead.springX.endValue = metrics.widthPixels - topChatHead!!.chatHead.width.toDouble() - (chatHeads.size - 1 - index) * (it.chatHead.width + CHAT_HEAD_EXPANDED_PADDING).toDouble()

                            it.chatHead.setOnTouchListener(onChatHeadTouch)
                        }

                        motionTrackerParams.flags = motionTrackerParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

                        params.flags = (params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        OverlayService.instance.windowManager.updateViewLayout(this, params)

                        topChatHead?.isSelected = true
                    }
                } else if (!toggled) {
                    moving = false

                    var xVelocity = velocityTracker!!.xVelocity.toDouble()
                    var yVelocity = velocityTracker!!.yVelocity.toDouble()
                    var maxVelocityX = 0.0


                    velocityTracker?.recycle()
                    velocityTracker = null

                    if (xVelocity < -3500) {
                        val newVelocity = ((-topChatHead!!.chatHead.springX.currentValue -  CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity - 5000
                        if (xVelocity > maxVelocityX)
                            xVelocity = newVelocity - 1500
                    } else if (xVelocity > 3500) {
                        val newVelocity = ((metrics.widthPixels - topChatHead!!.chatHead.springX.currentValue - topChatHead!!.chatHead.width + CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity + 5000
                        if (maxVelocityX > xVelocity)
                            xVelocity = newVelocity + 1500
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

                    if (xVelocity < 0) {
                        topChatHead?.chatHead?.springX?.velocity = max(xVelocity, maxVelocityX)
                    } else {
                        topChatHead?.chatHead?.springX?.velocity = min(xVelocity, maxVelocityX)
                    }

                    topChatHead?.chatHead?.springY?.velocity = yVelocity

                    topChatHead?.chatHead?.scaleX = 1f
                    topChatHead?.chatHead?.scaleY = 1f
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                    wasMoving = true
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