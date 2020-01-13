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

class Client(token: String) : ListenerAdapter() {
    companion object {
        lateinit var instance: Client
    }

    var user: SelfUser
    val jda: JDA

    init {
        jda = JDABuilder(AccountType.CLIENT).setToken(token)
            .addEventListeners(this)
            .build()

        instance = this

        user = jda.selfUser
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message

        var guildInfo: GuildInfo? = null

        val channel = event.channel

        if (event.channelType === ChannelType.TEXT) {
            if (event.guild.iconUrl != null) {
                // TODO: guild icon with first letter
                guildInfo = GuildInfo(event.guild.id, event.guild.name, event.guild.iconUrl!!, channel)
            }
        } else if (event.channelType === ChannelType.PRIVATE) {
            guildInfo = GuildInfo(
                event.privateChannel.id,
                event.privateChannel.name,
                getAvatarUrl(event.privateChannel.user),
                channel
            )
        }

        if (guildInfo != null) {
            runOnMainLoop {
                val chatHead = OverlayService.instance.chatHeads.add(guildInfo)
                if (chatHead.guildInfo.isServer && chatHead.guildInfo.channel.id == msg.channel.id || chatHead.guildInfo.isPrivate) {
                    if (OverlayService.instance.chatHeads.activeChatHead == chatHead) {
                        OverlayService.instance.chatHeads.content.addMessage(msg)
                    }
                }
            }
        }

    }
}