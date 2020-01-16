package com.sential.discordbubbles.client

import android.graphics.Bitmap
import android.graphics.Color
import com.sential.discordbubbles.chatheads.ChatHeads
import com.sential.discordbubbles.chatheads.OverlayService
import com.sential.discordbubbles.utils.*
import net.dv8tion.jda.api.entities.MessageChannel

class GuildInfo(val id: String, val name: String, avatarUrl: String?, val isPrivate: Boolean, var channelId: String) {
    var avatarUrl: String? = avatarUrl
        set(value) {
            field = value
            if (value != null) {
                Thread {
                    this.avatarBitmap = fetchBitmap(value)
                }.start()
            }
        }

    var channels: List<Channel> = emptyList()

    init {
        this.avatarUrl = avatarUrl

        val jda = OverlayService.instance.client?.jda
        val arr = if (isPrivate) {
            listOf(Channel(jda?.privateChannels?.find { it.id == id } as MessageChannel))
        } else {
            jda?.getGuildById(id)?.textChannels?.map { Channel(it) }
        }

        if (arr != null) channels = arr
    }

    val channel: Channel?
    get() = channels.find { it.id == channelId }

    var onAvatarChange: (() -> Unit)? = null

    var avatarBitmap: Bitmap? = null
    set(value) {
        field = value
        if (value != null) {
            chatHeadBitmap = value
                .addBackground(Color.WHITE)
                .makeCircular()
                .scaleToSize(ChatHeads.CHAT_HEAD_SIZE)
                .addShadow()

            if (onAvatarChange != null) {
                runOnMainLoop {
                    onAvatarChange!!()
                }
            }
        }
    }

    var chatHeadBitmap: Bitmap = Bitmap.createBitmap(ChatHeads.CHAT_HEAD_SIZE, ChatHeads.CHAT_HEAD_SIZE, Bitmap.Config.ARGB_8888)
        .addBackground(Color.WHITE)
        .makeCircular()
        .scaleToSize(ChatHeads.CHAT_HEAD_SIZE)
        .addShadow()
}
