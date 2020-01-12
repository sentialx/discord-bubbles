package com.sential.discordbubbles

import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.security.auth.login.LoginException


class Client : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message
        if (msg.contentRaw.equals("!ping")) {
            val channel = event.channel
            val time = System.currentTimeMillis()
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                .queue { response /* => Message */ ->
                    response.editMessageFormat(
                        "Pong: %d ms",
                        System.currentTimeMillis() - time
                    ).queue()
                }
        }
    }

    companion object {
        @Throws(LoginException::class)
        fun init() {
            JDABuilder(AccountType.CLIENT).setToken(DiscordTestToken.DISCORD_TOKEN)
                .addEventListeners(Client())
                .build()
        }
    }
}