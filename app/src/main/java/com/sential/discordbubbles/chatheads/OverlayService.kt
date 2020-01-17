package com.sential.discordbubbles.chatheads

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.view.*
import android.content.IntentFilter
import android.support.v4.app.NotificationCompat
import android.graphics.Color
import com.sential.discordbubbles.MainActivity
import android.os.Build
import android.support.annotation.RequiresApi
import android.widget.Toast
import com.sential.discordbubbles.R
import com.sential.discordbubbles.client.Client

class OverlayService : Service() {
    companion object {
        lateinit var instance: OverlayService
        var initialized = false
    }

    var token: String? = null

    lateinit var windowManager: WindowManager
    lateinit var chatHeads: ChatHeads

    var client: Client? = null

    private lateinit var innerReceiver: InnerReceiver

    override fun onCreate() {
        super.onCreate()

        instance = this
        initialized = true

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        chatHeads = ChatHeads(this)

        innerReceiver = InnerReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(innerReceiver, intentFilter)

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("overlay_service", "Discord Chat Heads service")
            } else {
                ""
            }

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setContentTitle("Discord chat heads are active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent).build()

        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        initialized = false
        client = null
        unregisterReceiver(innerReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && token == null) {
            token = intent.extras?.getString("token")
        }

        if (client == null) {
            if (token != null) {
                client = Client(token!!) {
                    Toast.makeText(
                        this, "Logged in successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return START_STICKY
    }
}

internal class InnerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS == action) {
            val reason = intent.getStringExtra("reason")
            if (reason != null) {
                OverlayService.instance.chatHeads.collapse()
            }
        }
    }
}