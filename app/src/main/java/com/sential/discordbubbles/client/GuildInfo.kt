package com.sential.discordbubbles.client

import android.graphics.Bitmap
import android.graphics.Color
import com.sential.discordbubbles.chatheads.ChatHeads
import com.sential.discordbubbles.utils.*
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageChannel


data class ChannelInfo(val id: String, val name: String)

class GuildInfo(val id: String, val name: String, avatarUrl: String, val channel: MessageChannel) {
    var avatarUrl: String = avatarUrl
        set(value) {
            field = value
            this.avatarBitmap = fetchBitmap(value)
        }

    init {
        this.avatarUrl = avatarUrl
    }

    var onAvatarChange: (() -> Unit)? = null

    val isPrivate: Boolean
    get() = channel.type == ChannelType.PRIVATE

    val isServer: Boolean
    get() = channel.type != ChannelType.PRIVATE

    var avatarBitmap: Bitmap? = null
    set(value) {
        field = value
        if (value != null) {
            chatHeadBitmap = value
                .addBackground(Color.WHITE)
                .makeCircular()
                .scaleToSize(ChatHeads.CHAT_HEAD_SIZE)
                .addShadow()
            onAvatarChange?.let { it() }
        }
    }

    var chatHeadBitmap: Bitmap? = null
}
