package com.sential.discordbubbles.chatheads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.sential.discordbubbles.*
import com.sential.discordbubbles.client.*
import com.sential.discordbubbles.utils.*
import kotlinx.android.synthetic.main.chat_head_content.view.*
import net.dv8tion.jda.api.entities.Message


class Content(context: Context): LinearLayout(context) {
    private val springSystem = SpringSystem.create()
    private val scaleSpring = springSystem.createSpring()

    private var channelView: TextView
    private var serverView: TextView
    private var hashTagView: TextView
    private var scrollView: ScrollView

    private var lastAuthorId: String? = null
    private var lastMessageGroup: View? = null

    private var messagesView: RelativeLayout

    init {
        inflate(context, R.layout.chat_head_content, this)

        channelView = findViewById(R.id.channel)
        serverView = findViewById(R.id.server)
        hashTagView = findViewById(R.id.hashtag)
        messagesView = findViewById(R.id.messages)
        scrollView = findViewById(R.id.scrollView)

        val editText: EditText = findViewById(R.id.editText)
        val sendBtn: LinearLayout = findViewById(R.id.chat_send)

        sendBtn.setOnClickListener {
            val bubble = OverlayService.instance.chatHeads.activeChatHead
            // TODO: add mentioning users
            bubble?.guildInfo?.channel?.sendMessage(editText.text)?.queue()
            editText.text.clear()
        }

        scaleSpring.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                scaleX = spring.currentValue.toFloat()
                scaleY = spring.currentValue.toFloat()
            }
        })
        scaleSpring.springConfig = SpringConfigs.CONTENT_SCALE

        scaleSpring.currentValue = 0.0
    }

    fun setInfo(chatHead: ChatHead) {
        if (chatHead.guildInfo.isPrivate) {
            serverView.visibility = GONE
            hashTagView.visibility = GONE
            channelView.text = chatHead.guildInfo.name
        } else {
            channelView.text = chatHead.guildInfo.channel.name
            serverView.text = chatHead.guildInfo.name
            serverView.visibility = VISIBLE
            hashTagView.visibility = VISIBLE
        }

        lastAuthorId = null
        lastMessageGroup = null

        messagesView.removeAllViews()

        chatHead.guildInfo.channel.history.retrievePast(50).queue { result ->
            runOnMainLoop {
                val arr = result.reversed()
                for (message in arr) {
                    addMessage(message, false)
                }
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    fun addMessage(message: Message, scrollToBottom: Boolean = true) {
        val view: View

        if (lastAuthorId != null && lastAuthorId == message.author.id && lastMessageGroup != null) {
            view = lastMessageGroup!!
        } else {
            view = inflate(context, R.layout.message_group, null)
            val root: LinearLayout = view.findViewById(R.id.group_root)
            root.id = View.generateViewId()

            if (message.author.avatarUrl != null) {
                Thread {
                    val bitmap = fetchBitmap(message.author.avatarUrl!!)?.makeCircular()

                    Handler(Looper.getMainLooper()).post {
                        view.findViewById<ImageView>(R.id.group_avatar).setImageBitmap(bitmap)
                    }
                }.start()
            }

            view.findViewById<TextView>(R.id.group_author).text = message.author.name

            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

            if (messagesView.childCount > 0) {
                val prev = messagesView.getChildAt(messagesView.childCount - 1)
                params.addRule(RelativeLayout.BELOW, prev.id)
                root.layoutParams = params
            } else {
                params.topMargin = dpToPx(8f)
                root.layoutParams = params
            }

            messages.addView(view)
        }

        val messagesView: LinearLayout = view.findViewById(R.id.group_messages)
        val messageView = inflate(context, R.layout.message, null)

        messageView.findViewById<TextView>(R.id.msg_body).text = message.contentRaw

        messagesView.addView(messageView)

        lastMessageGroup = view
        lastAuthorId = message.author.id

        if (scrollToBottom) {
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    fun hideContent() {
        OverlayService.instance.chatHeads.handler.removeCallbacks(
            OverlayService.instance.chatHeads.showContentRunnable)

        scaleSpring.endValue = 0.0

        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.duration = 200
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }

    fun showContent() {
        scaleSpring.endValue = 1.0

        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 100
        anim.repeatMode = Animation.RELATIVE_TO_SELF
        startAnimation(anim)
    }
}