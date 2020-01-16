package com.sential.discordbubbles.client

import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import com.sential.discordbubbles.chatheads.*
import com.sential.discordbubbles.utils.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.SelfUser

class Client(token: String, onLogin: (() -> Unit)? = null) : ListenerAdapter() {
    val jda: JDA = JDABuilder(AccountType.CLIENT).setToken(token)
        .addEventListeners(this)
        .build()

    init {
        Thread {
            jda.awaitReady()

            runOnMainLoop {
                if (onLogin != null) onLogin()
            }
        }.start()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message

        val guildInfo: GuildInfo

        val channel = Channel(event.channel)

        if (event.channelType == ChannelType.PRIVATE) {
            guildInfo = GuildInfo(
                event.privateChannel.id,
                event.privateChannel.name,
                getAvatarUrl(event.privateChannel.user),
                true,
                channel.id
            )
        } else {
            // TODO: guild icon with first letter
            guildInfo = GuildInfo(event.guild.id, event.guild.name, event.guild.iconUrl, false, channel.id)
        }

        runOnMainLoop {
            val chatHead = OverlayService.instance.chatHeads.add(guildInfo)
            chatHead.guildInfo.channels.find { it.id == event.channel.id }?.addMessage(msg)
            chatHead.updateNotifications()
        }
    }
}