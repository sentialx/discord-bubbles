package com.sential.discordbubbles.client

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageChannel

class Channel(messageChannel: MessageChannel) {
    val type = messageChannel.type

    val id = messageChannel.id

    val instance: MessageChannel
    get() {
        return if (type == ChannelType.PRIVATE) {
            Client.instance.jda.getPrivateChannelById(id) as MessageChannel
        } else {
            Client.instance.jda.getTextChannelById(id) as MessageChannel
        }
    }
}