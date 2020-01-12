package com.sential.discordbubbles

import android.os.Handler
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import android.os.Looper
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.SelfUser


class Client : ListenerAdapter() {
    companion object {
        lateinit var instance: Client
    }

    var user: SelfUser

    init {
        val jda = JDABuilder(AccountType.CLIENT).setToken(DiscordTestToken.DISCORD_TOKEN)
            .addEventListeners(this)
            .build()

        instance = this

        user = jda.selfUser
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message

        var guildInfo: GuildInfo? = null

        if (event.channelType === ChannelType.TEXT) {
            if (event.guild.iconUrl != null) {
                guildInfo = GuildInfo(event.guild.id, event.guild.name, event.guild.iconUrl!!, ChannelInfo(event.channel.id, event.channel.name))
            }
        } else if (event.channelType === ChannelType.PRIVATE) {
            if (event.privateChannel.user.avatarUrl != null) {
                guildInfo = GuildInfo(
                    event.privateChannel.id,
                    event.privateChannel.name,
                    event.privateChannel.user.avatarUrl!!
                )
            }
        }

        if (guildInfo != null) {
            Handler(Looper.getMainLooper()).post {
                val chatHead = OverlayService.instance.chatHeads.add(guildInfo)
                chatHead.messages.add(Message(msg.author, msg.contentRaw, msg.channel.id))
                OverlayService.instance.chatHeads.updateActiveContent()
            }
        }

    }
}