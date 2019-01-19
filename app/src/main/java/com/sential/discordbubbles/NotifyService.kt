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
            val title = sbn.notification.extras.getString("android.title") ?: return

            val regex = Regex("#[^ ]+\\b")
            val matchResults = regex.findAll(title!!)

            val match = matchResults.lastOrNull()

            var server: String? = title.split(":")[0]
            var channel: String? = null

            if (match != null) {
                server = title.substring(0, match.range.first)
                channel = matchResults.last().value.substring(1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messages = sbn.notification.extras.get("android.messages") as Array<Parcelable>
                val msgBundle = messages.last() as Bundle

                if (server != null) {
                    var chatHead = OverlayService.instance.chatHeads.chatHeads.find { it.server == server }

                    if (chatHead == null) {
                        chatHead = OverlayService.instance.chatHeads.add(server, channel)
                    }

                    val msg = Message(msgBundle.getString("sender")!!, msgBundle.get("text")!!.toString(), channel)
                    chatHead.messages.add(msg)

                    if (chatHead.isActive) {
                        OverlayService.instance.chatHeads.content.addMessage(msg)
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}