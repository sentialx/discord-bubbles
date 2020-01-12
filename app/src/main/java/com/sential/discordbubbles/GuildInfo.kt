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
            println(value)
            try {
                val url = java.net.URL(value)
                val connection = url
                    .openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                this.avatarBitmap = BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
            }
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

            chatHeadBitmap = ImageHelper.addShadow(ImageHelper.getCircularBitmap(value))
            onAvatarChange?.let { it() }
        }
    }

    var chatHeadBitmap: Bitmap? = null
}
