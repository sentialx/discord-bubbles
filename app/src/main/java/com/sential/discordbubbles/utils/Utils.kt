package com.sential.discordbubbles.utils

import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.util.TypedValue
import net.dv8tion.jda.api.entities.User

fun getOverlayFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }
}

fun getScreenSize(): DisplayMetrics {
    return Resources.getSystem().displayMetrics
}

fun dpToPx(dp: Float): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun spToPx(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().displayMetrics)
}

fun runOnMainLoop(fn: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        fn()
    }
}

fun getAvatarUrl(user: User): String {
    return if (user.avatarUrl != null) {
        user.avatarUrl!!
    } else {
        user.defaultAvatarUrl
    }
}

fun debug(txt: String) {
    println("asdf: $txt")
}