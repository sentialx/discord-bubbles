package com.sential.discordbubbles.client

import net.dv8tion.jda.api.entities.User
import java.time.OffsetDateTime

data class Message(val author: User, val body: String, val time: OffsetDateTime, val channel: String)
