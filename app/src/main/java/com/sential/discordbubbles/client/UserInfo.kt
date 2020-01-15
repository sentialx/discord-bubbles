package com.sential.discordbubbles.client

import android.graphics.Bitmap
import net.dv8tion.jda.api.entities.User

class UserInfo(user: User) {
    val id = user.id

    val name = user.name

    val avatarUrl: String = if (user.avatarUrl != null) user.avatarUrl!! else user.defaultAvatarUrl

    val avatarId: String = if (user.avatarId != null) user.avatarId!! else user.defaultAvatarId

    var avatarBitmap: Bitmap? = null
    set(value) {
        field = value
        if (onAvatarChange != null) onAvatarChange!!(value)
    }

    var onAvatarChange: ((bmp: Bitmap?) -> Unit)? = null
}