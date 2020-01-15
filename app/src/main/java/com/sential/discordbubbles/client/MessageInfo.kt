package com.sential.discordbubbles.client

import net.dv8tion.jda.api.entities.Message

class MessageInfo(message: Message) {
    val id = message.id

    val text = message.contentRaw

    val author = UserInfo(message.author)
}