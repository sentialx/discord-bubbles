package com.sential.discordbubbles

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.os.IBinder
import android.os.Bundle
import android.os.Parcelable
import android.os.Build
import android.R.attr.keySet
import android.util.Log


class NotifyService : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
    }

    private fun getString(str: String?): String {
        return if (str == null) {
            "null"
        } else {
            str
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == "com.discord") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val b = sbn.notification.extras.get("android.messages") as Array<Parcelable>

                val msgBundle = b.last() as Bundle
                OverlayService.instance.overlayLayout.addChatItem(getString(msgBundle.getString("sender")), getString(msgBundle.get("text")!!.toString()))
            }


        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}