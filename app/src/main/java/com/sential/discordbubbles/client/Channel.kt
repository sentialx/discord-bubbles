package com.sential.discordbubbles.client

import com.sential.discordbubbles.chatheads.OverlayService
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageChannel

class Channel(messageChannel: MessageChannel) {
    val type = messageChannel.type

    val id = messageChannel.id

    val instance: MessageChannel
    get() {
        return if (type == ChannelType.PRIVATE) {
            OverlayService.instance.client?.jda?.getPrivateChannelById(id) as MessageChannel
        } else {
            OverlayService.instance.client?.jda?.getTextChannelById(id) as MessageChannel
        }
    }
}