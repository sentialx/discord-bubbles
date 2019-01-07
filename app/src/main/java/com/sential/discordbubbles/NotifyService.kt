package com.sential.discordbubbles

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.os.IBinder
import android.os.Bundle
import android.os.Parcelable
import android.os.Build

class NotifyService : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == "com.discord") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messages = sbn.notification.extras.get("android.messages") as Array<Parcelable>
                val msgBundle = messages.last() as Bundle

                // OverlayService.instance.chatHeadLayout.addChatItem(msgBundle.getString("sender")!!, msgBundle.get("text")!!.toString())
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}