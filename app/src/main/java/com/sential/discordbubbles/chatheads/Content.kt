package com.sential.discordbubbles.chatheads

import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.sential.discordbubbles.*
import com.sential.discordbubbles.utils.*
import kotlinx.android.synthetic.main.chat_head_content.view.*
import net.dv8tion.jda.api.entities.Message
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.sential.discordbubbles.client.MessageInfo
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.User

class ListenableMessage(val message: Message) {
    var onAvatarChange: ((it: Bitmap?) -> Unit)? = null
}

class Content(context: Context): LinearLayout(context) {
    private val springSystem = SpringSystem.create()
    private val scaleSpring = springSystem.createSpring()

    private var channelView: TextView
    private var hashTagView: TextView
    private var atView: TextView

    var messagesView: RecyclerView
    var messagesAdapter = ChatAdapter(this.context, emptyList())
    var layoutManager = LinearLayoutManager(context)

    init {
        inflate(context, R.layout.chat_head_content, this)

        channelView = findViewById(R.id.channel)
        hashTagView = findViewById(R.id.hashtag)
        atView = findViewById(R.id.at)
        messagesView = findViewById(R.id.messages)

        layoutManager.stackFromEnd = true

        messagesView.layoutManager = layoutManager
        messagesView.adapter = messagesAdapter

        messagesView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (layoutManager.findLastVisibleItemPosition() == messagesAdapter.messages.lastIndex) {
                OverlayService.instance.chatHeads.activeChatHead?.notifications = 0
            }
        }

        val editText: EditText = findViewById(R.id.editText)
        val sendBtn: LinearLayout = findViewById(R.id.chat_send)
        val openExternalBtn: LinearLayout = findViewById(R.id.open_external)

        openExternalBtn.setOnClickListener {
            val bubble = OverlayService.instance.chatHeads.activeChatHead
            if (bubble != null) {
                val scope = if (bubble.guildInfo.channel.type == ChannelType.PRIVATE) "@me" else bubble.guildInfo.id
                val url = "https://discordapp.com/channels/$scope/${bubble.guildInfo.channel.id}"
                launchDiscord(url)
            }
        }

        sendBtn.setOnClickListener {
            val bubble = OverlayService.instance.chatHeads.activeChatHead
            if (!editText.text.isNullOrEmpty()) {
                // TODO: add mentioning users
                bubble?.guildInfo?.channel?.instance?.sendMessage(editText.text)?.queue()
                editText.text.clear()
            }
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
        if (chatHead.guildInfo.channel.type == ChannelType.PRIVATE) {
            atView.visibility = View.VISIBLE
            hashTagView.visibility = View.GONE
            channelView.text = chatHead.guildInfo.name
        } else {
            atView.visibility = View.GONE
            hashTagView.visibility = View.VISIBLE
            channelView.text = chatHead.guildInfo.channel.instance.name
        }

        if (chatHead.messages.size == 0) {
            Thread {
                chatHead.guildInfo.channel.instance.history.retrievePast(50).queue { result ->
                    runOnMainLoop {
                        chatHead.addMessages(result.reversed())
                    }
                }
            }.start()
        }

        messagesAdapter.messages = chatHead.messages
        messagesAdapter.notifyDataSetChanged()
        messagesView.scrollToPosition(messagesAdapter.messages.lastIndex)
    }

    fun launchDiscord(url: String) {
        val packageName = "com.discord"
        if (isAppInstalled(context, packageName))
            if (isAppEnabled(context, packageName)) {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                    OverlayService.instance.chatHeads.collapse()
                }
            }
            else
                Toast.makeText(context, "Discord app is not enabled.", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context, "Discord app is not installed.", Toast.LENGTH_SHORT).show()
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return false
    }

    private fun isAppEnabled(context: Context, packageName: String): Boolean {
        var appStatus = false
        try {
            val ai = context.packageManager.getApplicationInfo(packageName, 0)
            if (ai != null) {
                appStatus = ai.enabled
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return appStatus
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