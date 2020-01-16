package com.sential.discordbubbles.client

import android.graphics.Bitmap
import android.graphics.Color
import com.sential.discordbubbles.chatheads.ChatHeads
import com.sential.discordbubbles.chatheads.OverlayService
import com.sential.discordbubbles.utils.*

class GuildInfo(val id: String, val name: String, avatarUrl: String?, val channel: Channel) {
    var avatarUrl: String? = avatarUrl
        set(value) {
            field = value
            if (value != null) {
                Thread {
                    this.avatarBitmap = fetchBitmap(value)
                }.start()
            }
        }

    init {
        this.avatarUrl = avatarUrl
    }

    val channels = OverlayService.instance.client?.jda?.getGuildById(id)?.textChannels?.map { Channel(it) }

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
