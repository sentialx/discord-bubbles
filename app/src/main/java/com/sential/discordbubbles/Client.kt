package com.sential.discordbubbles

import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.security.auth.login.LoginException


class Client : ListenerAdapter() {
    init {
        JDABuilder(AccountType.CLIENT).setToken(DiscordTestToken.DISCORD_TOKEN)
            .addEventListeners(this)
            .build()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message
        print("test " + msg.contentRaw)
    }
}