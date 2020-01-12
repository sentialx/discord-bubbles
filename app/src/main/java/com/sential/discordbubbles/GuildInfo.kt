package com.sential.discordbubbles

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection


data class ChannelInfo(val id: String, val name: String)

class GuildInfo(val id: String, val name: String, avatarUrl: String, val channel: ChannelInfo? = null) {
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
    get() = channel == null

    val isServer: Boolean
    get() = channel != null

    var avatarBitmap: Bitmap? = null
    set(value) {
        field = value
        if (value != null) {
            chatHeadBitmap = value.addBackground().makeCircular().scaleToSize(ChatHeads.CHAT_HEAD_SIZE).addShadow()
            onAvatarChange?.let { it() }
        }
    }

    var chatHeadBitmap: Bitmap? = null
}
