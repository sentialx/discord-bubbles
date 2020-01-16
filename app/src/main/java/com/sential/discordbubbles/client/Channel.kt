package com.sential.discordbubbles.client

import android.graphics.Bitmap
import com.sential.discordbubbles.chatheads.OverlayService
import com.sential.discordbubbles.utils.fetchBitmap
import com.sential.discordbubbles.utils.makeCircular
import com.sential.discordbubbles.utils.runOnMainLoop
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel

class Channel(messageChannel: MessageChannel) {
    val type = messageChannel.type

    val id = messageChannel.id

    val instance: MessageChannel
    get() {
        val jda = OverlayService.instance.client?.jda
        return if (type == ChannelType.PRIVATE) {
            jda?.getPrivateChannelById(id) as MessageChannel
        } else {
            jda?.getTextChannelById(id) as MessageChannel
        }
    }

    var baseHistoryLoaded = false

    var notifications: Int = 0

    var messages = mutableListOf<MessageInfo>()

    private var avatarsCache = mutableMapOf<String, Bitmap?>()

    fun cacheAvatar(user: UserInfo): Bitmap? {
        return if (avatarsCache[user.avatarId] == null) {
            val bmp = fetchBitmap(user.avatarUrl)?.makeCircular()
            avatarsCache[user.avatarId] = bmp
            bmp
        } else {
            avatarsCache[user.avatarId]
        }
    }

    fun clearMessages() {
        val adapter = OverlayService.instance.chatHeads.content.messagesAdapter
        adapter.messages = emptyList()
        adapter.notifyDataSetChanged()
    }

    fun addMessages(msgs: List<Message>) {
        notifications += msgs.size

        if (!baseHistoryLoaded) return

        val infos = ArrayList<MessageInfo>()
        val chatHeads = OverlayService.instance.chatHeads

        for (message in msgs) {
            val info = MessageInfo(message)
            infos.add(info)
            messages.add(info)
        }

        Thread {
            for (info in infos) {
                val bmp = cacheAvatar(info.author)
                runOnMainLoop {
                    info.author.avatarBitmap = bmp
                }
            }
        }.start()

        if (chatHeads.activeChatHead?.guildInfo?.channelId == id) {
            val adapter = chatHeads.content.messagesAdapter
            val lm = chatHeads.content.layoutManager
            val startIndex = adapter.messages.lastIndex
            adapter.messages = messages
            adapter.notifyItemRangeInserted(startIndex, adapter.messages.lastIndex)

            if (lm.findLastVisibleItemPosition() >= startIndex - 1) {
                chatHeads.content.messagesView.smoothScrollToPosition(adapter.messages.lastIndex)
            }
        }
    }

    fun addMessage(msg: Message) {
        addMessages(listOf(msg))
    }
}