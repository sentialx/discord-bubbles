package com.sential.discordbubbles

import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlin.math.pow
import android.view.VelocityTracker
import com.facebook.rebound.Spring
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.SpringChain
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ChatHeads(context: Context) : View.OnTouchListener, FrameLayout(context) {
    companion object {
        val CHAT_HEAD_OUT_OF_SCREEN_X: Int = WindowManagerHelper.dpToPx(10f)
        val CHAT_HEAD_SIZE: Int = WindowManagerHelper.dpToPx(64f)
        val CHAT_HEAD_PADDING: Int = WindowManagerHelper.dpToPx(6f)
        val CHAT_HEAD_EXPANDED_PADDING: Int = WindowManagerHelper.dpToPx(4f)
        val CHAT_HEAD_EXPANDED_MARGIN_TOP: Float = WindowManagerHelper.dpToPx(4f).toFloat()

        const val CHAT_HEAD_DRAG_TOLERANCE: Float = 20f

        fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
            return ((x1 - x2).pow(2) + (y1-y2).pow(2))
        }
    }

    var chatHeads = ArrayList<ChatHead>()

    private var initialX = 0.0f
    private var initialY = 0.0f

    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f

    private var initialVelocityX = 0.0
    private var initialVelocityY = 0.0

    private var lastY = 0.0

    private var moving = false
    private var wasMoving = false
    private var toggled = false
    private var motionTrackerUpdated = false
    private var collapsing = false

    private var horizontalSpringChain: SpringChain? = null
    private var verticalSpringChain: SpringChain? = null

    private var isOnRight = false

    private var velocityTracker: VelocityTracker? = null

    private var motionTracker = LinearLayout(context)

    var topChatHead: ChatHead? = null
    var content = Content(context)

    private var motionTrackerParams = WindowManager.LayoutParams(
        CHAT_HEAD_SIZE,
        CHAT_HEAD_SIZE,
        WindowManagerHelper.getLayoutFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    )

    private var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManagerHelper.getLayoutFlag(),
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    )

    init {
        params.gravity = Gravity.START or Gravity.TOP
        params.dimAmount = 0.7f

        motionTrackerParams.gravity = Gravity.START or Gravity.TOP

        OverlayService.instance.windowManager.addView(motionTracker, motionTrackerParams)
        OverlayService.instance.windowManager.addView(this, params)
        this.addView(content)

        motionTracker.setOnTouchListener(this)

        this.setOnTouchListener{ v, event ->
            v.performClick()

            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (v == this) {
                        collapse()
                    }
                }

            }

            return@setOnTouchListener false
        }
    }

    fun setTop(chatHead: ChatHead) {
        topChatHead?.isTop = false
        chatHead.isTop = true

        topChatHead = chatHead
    }

    fun fixPositions(animation: Boolean = true) {
        if (topChatHead == null) return

        val metrics = WindowManagerHelper.getScreenSize()

        val newX =  if (isOnRight) metrics.widthPixels - topChatHead!!.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble() else -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
        val newY = initialY.toDouble()

        if (animation) {
            topChatHead!!.springX.endValue = newX
            topChatHead!!.springY.endValue = newY
        } else {
            topChatHead!!.springX.currentValue = newX
            topChatHead!!.springY.currentValue = newY
        }
    }

    private fun destroySpringChains() {
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

        verticalSpringChain = null
        horizontalSpringChain = null
    }


    private fun resetSpringChains() {
       destroySpringChains()

        horizontalSpringChain = SpringChain.create(0, 0, 200, 15)
        verticalSpringChain = SpringChain.create(0, 0, 200, 15)

        chatHeads.forEachIndexed { index, element ->
            element.z = index.toFloat()

            if (element.isTop) {
                horizontalSpringChain!!.addSpring(object : SimpleSpringListener() { })
                verticalSpringChain!!.addSpring(object : SimpleSpringListener() { })

                element.z = chatHeads.size.toFloat()
                horizontalSpringChain!!.setControlSpringIndex(index)
                verticalSpringChain!!.setControlSpringIndex(index)
            } else {
                horizontalSpringChain!!.addSpring(object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring?) {
                        if (!toggled) {
                            if (collapsing) {
                                element.springX.endValue = spring!!.currentValue + (chatHeads.size - 1 - index) * CHAT_HEAD_PADDING * if (isOnRight) 1 else -1
                            } else {
                                element.springX.currentValue = spring!!.currentValue + (chatHeads.size - 1 - index) * CHAT_HEAD_PADDING * if (isOnRight) 1 else -1
                            }
                        }
                    }
                })
                verticalSpringChain!!.addSpring(object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring?) {
                        if (!toggled) {
                            element.springY.currentValue = spring!!.currentValue
                        }
                    }
                })
            }
        }
    }

    fun add(server: String, channel: String? = null): ChatHead {
        val chatHead = ChatHead(this, server, channel)
        chatHeads.add(chatHead)

        var lx = 0.0
        var ly = 0.0

        if (topChatHead != null) {
            lx = topChatHead!!.springX.currentValue
            ly = topChatHead!!.springY.currentValue
        }

        setTop(chatHead)
        destroySpringChains()
        resetSpringChains()

        toggled = true

        chatHeads.forEachIndexed { index, element ->
            element.springX.currentValue = lx + (chatHeads.size - 1 - index) * CHAT_HEAD_PADDING * if (isOnRight) 1 else -1
            element.springY.currentValue = ly
        }

        return chatHead
    }

    fun collapse() {
        toggled = false
        collapsing = true

        fixPositions()

        chatHeads.forEach {
            it.isActive = false
        }

        content.hideContent()

        motionTrackerParams.flags = motionTrackerParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

        params.flags = ((params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()) and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv() or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        OverlayService.instance.windowManager.updateViewLayout(this, params)
    }

    fun changeContent() {
        val chatHead = chatHeads.find { it.isActive }!!

        content.messagesView.removeAllViews()
        content.lastId = 0

        content.channel = chatHead.channel
        content.server = chatHead.server

        for (message in chatHead.messages) {
            content.addMessage(message)
        }
    }

    fun onSpringUpdate(chatHead: ChatHead, spring: Spring, totalVelocity: Int) {
        val metrics = WindowManagerHelper.getScreenSize()

        if (topChatHead != null && chatHead == topChatHead!!) {
            if (horizontalSpringChain != null && spring == chatHead.springX) {
                horizontalSpringChain!!.controlSpring.currentValue = spring.currentValue
            }

            if (verticalSpringChain != null && spring == chatHead.springY) {
                verticalSpringChain!!.controlSpring.currentValue = spring.currentValue
            }
        }

        var tmpChatHead: ChatHead? = null
        if (collapsing) tmpChatHead = topChatHead!!
        else if (chatHead.isActive) tmpChatHead = chatHead

        if (tmpChatHead != null) {
            content.x = tmpChatHead.springX.currentValue.toFloat() - metrics.widthPixels.toFloat() + ((chatHeads.size - 1 - chatHeads.indexOf(tmpChatHead)) * (tmpChatHead.width + CHAT_HEAD_EXPANDED_PADDING)) + tmpChatHead.width
            content.y = tmpChatHead.springY.currentValue.toFloat() - CHAT_HEAD_EXPANDED_MARGIN_TOP
            content.pivotX = metrics.widthPixels.toFloat() - chatHead.width / 2 - ((chatHeads.size - 1 - chatHeads.indexOf(tmpChatHead)) * (tmpChatHead.width + CHAT_HEAD_EXPANDED_PADDING))
        }

        content.pivotY = chatHead.height.toFloat()

        if (wasMoving) {
            motionTrackerParams.x = if (isOnRight) metrics.widthPixels - chatHead.width else 0

            lastY = chatHead.springY.currentValue

            if (abs(chatHead.springY.velocity) > 3000 && (chatHead.springX.currentValue > metrics.widthPixels - chatHead.width + CHAT_HEAD_OUT_OF_SCREEN_X / 2 || chatHead.springX.currentValue < -CHAT_HEAD_OUT_OF_SCREEN_X / 2) && abs(initialVelocityX) > 3000) {
                chatHead.springY.velocity = 3000.0 * if (initialVelocityY < 0) -1 else 1
            }

            if ((chatHead.springX.currentValue < -CHAT_HEAD_OUT_OF_SCREEN_X / 2 && initialVelocityX < -3000 || chatHead.springX.currentValue > metrics.widthPixels - chatHead.width  + CHAT_HEAD_OUT_OF_SCREEN_X / 2) && abs(initialVelocityY) < abs(initialVelocityX)) {
                chatHead.springY.velocity = 0.0
            }

            if (abs(chatHead.springY.velocity) > 500) {
                if (chatHead.springY.currentValue < 0) {
                    chatHead.springY.velocity = -500.0
                } else if (chatHead.springY.currentValue > metrics.heightPixels) {
                    chatHead.springY.velocity = 500.0
                }
            }

            if (!moving) {
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

                motionTrackerParams.y = chatHead.springY.endValue.toInt()

                OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
            }

            if (Math.abs(totalVelocity) < WindowManagerHelper.dpToPx(10f) && !moving) {
                motionTrackerParams.y = chatHead.springY.currentValue.toInt() + chatHead.springY.velocity.toInt()

                OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val metrics = WindowManagerHelper.getScreenSize()

        if (topChatHead == null) return true

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = topChatHead!!.springX.currentValue.toFloat()
                initialY = topChatHead!!.springY.currentValue.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                wasMoving = false
                collapsing = false
                toggled = false

                topChatHead!!.scaleX = 0.9f
                topChatHead!!.scaleY = 0.9f

                topChatHead!!.springX.springConfig = SpringConfigs.DRAGGING
                topChatHead!!.springY.springConfig = SpringConfigs.DRAGGING

                topChatHead!!.springX.setAtRest()
                topChatHead!!.springY.setAtRest()

                motionTrackerUpdated = false

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain()
                } else {
                    velocityTracker?.clear()
                }

                motionTrackerParams.x = topChatHead!!.springX.currentValue.toInt()
                motionTrackerParams.y = topChatHead!!.springY.currentValue.toInt()

                OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_UP -> {
                if (!moving) {
                    if (!toggled) {
                        toggled = true

                        chatHeads.forEachIndexed { index, it ->
                            it.springX.springConfig = SpringConfigs.NOT_DRAGGING
                            it.springY.springConfig = SpringConfigs.NOT_DRAGGING

                            it.springY.endValue = CHAT_HEAD_EXPANDED_MARGIN_TOP.toDouble()
                            it.springX.endValue = metrics.widthPixels - topChatHead!!.width.toDouble() - (chatHeads.size - 1 - index) * (it.width + CHAT_HEAD_EXPANDED_PADDING).toDouble()
                        }

                        motionTrackerParams.flags = motionTrackerParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        OverlayService.instance.windowManager.updateViewLayout(motionTracker, motionTrackerParams)

                        params.flags = (params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()) or WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                        OverlayService.instance.windowManager.updateViewLayout(this, params)

                        topChatHead!!.isActive = true

                        changeContent()

                        android.os.Handler().postDelayed(
                            {
                                content.showContent()
                            }, 200
                        )
                    }
                } else if (!toggled) {
                    moving = false

                    var xVelocity = velocityTracker!!.xVelocity.toDouble()
                    var yVelocity = velocityTracker!!.yVelocity.toDouble()
                    var maxVelocityX = 0.0

                    velocityTracker?.recycle()
                    velocityTracker = null

                    if (xVelocity < -3500) {
                        val newVelocity = ((-topChatHead!!.springX.currentValue -  CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity - 5000
                        if (xVelocity > maxVelocityX)
                            xVelocity = newVelocity - 500
                    } else if (xVelocity > 3500) {
                        val newVelocity = ((metrics.widthPixels - topChatHead!!.springX.currentValue - topChatHead!!.width + CHAT_HEAD_OUT_OF_SCREEN_X) * SpringConfigs.DRAGGING.friction)
                        maxVelocityX = newVelocity + 5000
                        if (maxVelocityX > xVelocity)
                            xVelocity = newVelocity + 500
                    } else if (yVelocity > 20 || yVelocity < -20) {
                        topChatHead!!.springX.springConfig = SpringConfigs.NOT_DRAGGING

                        if (topChatHead!!.x >= metrics.widthPixels / 2) {
                            topChatHead!!.springX.endValue = metrics.widthPixels - topChatHead!!.width + CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            isOnRight = true
                        } else {
                            topChatHead!!.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()

                            isOnRight = false
                        }
                    } else {
                        topChatHead!!.springX.springConfig = SpringConfigs.NOT_DRAGGING
                        topChatHead!!.springY.springConfig = SpringConfigs.NOT_DRAGGING

                        if (topChatHead!!.x >= metrics.widthPixels / 2) {
                            topChatHead!!.springX.endValue = metrics.widthPixels - topChatHead!!.width +
                                    CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            topChatHead!!.springY.endValue = topChatHead!!.y.toDouble()

                            isOnRight = true
                        } else {
                            topChatHead!!.springX.endValue = -CHAT_HEAD_OUT_OF_SCREEN_X.toDouble()
                            topChatHead!!.springY.endValue = topChatHead!!.y.toDouble()

                            isOnRight = false
                        }
                    }

                    if (xVelocity < 0) {
                        topChatHead!!.springX.velocity = max(xVelocity, maxVelocityX)
                    } else {
                        topChatHead!!.springX.velocity = min(xVelocity, maxVelocityX)
                    }

                    initialVelocityX = topChatHead!!.springX.velocity
                    initialVelocityY = topChatHead!!.springY.velocity

                    topChatHead!!.springY.velocity = yVelocity
                }

                topChatHead!!.scaleX = 1f
                topChatHead!!.scaleY = 1f
            }
            MotionEvent.ACTION_MOVE -> {
                if (distance(initialTouchX, event.rawX, initialTouchY, event.rawY) > CHAT_HEAD_DRAG_TOLERANCE.pow(2)) {
                    moving = true
                    wasMoving = true
                }

                velocityTracker?.addMovement(event)

                if (moving) {
                    topChatHead!!.springX.currentValue = initialX + (event.rawX - initialTouchX).toDouble()
                    topChatHead!!.springY.currentValue = initialY + (event.rawY - initialTouchY).toDouble()

                    velocityTracker?.computeCurrentVelocity(2000)
                }
            }
        }

        return true
    }
}