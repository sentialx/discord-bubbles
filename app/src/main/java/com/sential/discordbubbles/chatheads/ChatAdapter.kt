package com.sential.discordbubbles.chatheads

import android.content.Context
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.sential.discordbubbles.R
import com.sential.discordbubbles.client.*

class ChatAdapter(
    private val context: Context,
    var messages: List<MessageInfo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return if (position == 0 || message.author.name != messages[position - 1].author.name) {
            VIEW_TYPE_MESSAGE_HEADER
        } else {
            VIEW_TYPE_MESSAGE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_MESSAGE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_header, parent, false)
            return HeaderMessageHolder(view)
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message, parent, false)
        return MessageHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_HEADER -> (holder as HeaderMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_TEXT -> (holder as MessageHolder).bind(message)
        }
    }

    private inner class MessageHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {
        private val messageTextView: TextView = view.findViewById(R.id.message_body)

        internal fun bind(message: MessageInfo) {
            messageTextView.text = message.text
        }
    }

    private inner class HeaderMessageHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view) {
        private val messageTextView: TextView = view.findViewById(R.id.message_header_body)
        private val authorTextView: TextView = view.findViewById(R.id.message_header_author)
        private var avatarImageView: ImageView = view.findViewById(R.id.message_header_avatar)

        internal fun bind(message: MessageInfo) {
            messageTextView.text = message.text

            authorTextView.text = message.author.name

            avatarImageView.setImageBitmap(message.author.avatarBitmap)

            message.author.onAvatarChange = {
                avatarImageView.setImageBitmap(it)
            }
        }
    }

    companion object {
        private val VIEW_TYPE_MESSAGE_HEADER = 1
        private val VIEW_TYPE_MESSAGE_TEXT = 2
    }
}