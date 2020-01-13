package com.sential.discordbubbles.client

import android.os.Handler
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import android.os.Looper
import com.sential.discordbubbles.chatheads.*
import com.sential.discordbubbles.utils.runOnMainLoop
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.MessageChannel



class Client : ListenerAdapter() {
    companion object {
        lateinit var instance: Client
    }

    var user: SelfUser
    val jda: JDA

    init {
        jda = JDABuilder(AccountType.CLIENT).setToken(DiscordTestToken.DISCORD_TOKEN)
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
                guildInfo = GuildInfo(event.guild.id, event.guild.name, event.guild.iconUrl!!, channel)
            }
        } else if (event.channelType === ChannelType.PRIVATE) {
            if (event.privateChannel.user.avatarUrl != null) {
                guildInfo = GuildInfo(
                    event.privateChannel.id,
                    event.privateChannel.name,
                    event.privateChannel.user.avatarUrl!!,
                    channel
                )
            }
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