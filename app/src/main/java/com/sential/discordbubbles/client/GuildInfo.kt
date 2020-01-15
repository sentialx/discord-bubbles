package com.sential.discordbubbles.client

import android.graphics.Bitmap
import android.graphics.Color
import com.sential.discordbubbles.chatheads.ChatHeads
import com.sential.discordbubbles.utils.*

class GuildInfo(val id: String, val name: String, avatarUrl: String, val channel: Channel) {
    var avatarUrl: String = avatarUrl
        set(value) {
            field = value
            this.avatarBitmap = fetchBitmap(value)
        }

    init {
        this.avatarUrl = avatarUrl
    }

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
                onAvatarChange!!()
            }
        }
    }

    var chatHeadBitmap: Bitmap? = null
}
