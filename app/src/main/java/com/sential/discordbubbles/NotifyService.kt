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
            val title = sbn.notification.extras.getString("android.title")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messages = sbn.notification.extras.get("android.messages") as Array<Parcelable>
                val msgBundle = messages.last() as Bundle

                var chatHead = OverlayService.instance.chatHeads.chatHeads.find { it.server == title }

                if (chatHead == null) {
                    chatHead = OverlayService.instance.chatHeads.add(true)
                }

                //chatHead.chatHeadLayout.addChatItem(msgBundle.getString("sender")!!, msgBundle.get("text")!!.toString())
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}