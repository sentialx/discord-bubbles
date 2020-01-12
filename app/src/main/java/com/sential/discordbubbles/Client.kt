package com.sential.discordbubbles

import android.os.Handler
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.security.auth.login.LoginException
import android.os.Looper
import net.dv8tion.jda.api.entities.ChannelType


class Client : ListenerAdapter() {
    init {
        JDABuilder(AccountType.CLIENT).setToken(DiscordTestToken.DISCORD_TOKEN)
            .addEventListeners(this)
            .build()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message

        Handler(Looper.getMainLooper()).post(Runnable {
            var bubbleType: BubbleType = BubbleType.GUILD
            var bubbleServer: String = ""
            var bubbleChannel: String = ""

            if (event.channelType === ChannelType.TEXT) {
                bubbleServer = event.guild.name
                bubbleChannel = event.channel.name
            } else if (event.channelType === ChannelType.PRIVATE) {
                bubbleType = BubbleType.DM
                bubbleServer = event.privateChannel.name
                bubbleChannel = ""
            }

            val chatHead = OverlayService.instance.chatHeads.add(bubbleType, bubbleServer, bubbleChannel)
            chatHead.messages.add(Message(msg.author.id, msg.contentRaw, msg.channel.id))
            OverlayService.instance.chatHeads.changeContent()
        })

    }
}