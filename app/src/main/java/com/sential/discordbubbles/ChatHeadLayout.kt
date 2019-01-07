package com.sential.discordbubbles

import android.graphics.Color
import android.graphics.PixelFormat
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView


class ChatHeadLayout(container: ChatHeadContainer) {
    var params: WindowManager.LayoutParams

    var view: View = LayoutInflater.from(OverlayService.instance).inflate(R.layout.chat, null)
    var darkBackground: LinearLayout
    var editText: EditText
    var contentLayout: LinearLayout
    var chat: LinearLayout

    private val onKey: View.OnKeyListener = View.OnKeyListener { _, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //OverlayService.instance.hide()
            return@OnKeyListener true
        }
        return@OnKeyListener false
    }

    init {
        view.visibility = View.GONE

        editText = view.findViewById(R.id.editText)

        darkBackground = view.findViewById(R.id.darkBg)
        darkBackground.setOnClickListener {
           // OverlayService.instance.hide()
        }

        chat = view.findViewById(R.id.chat)

        contentLayout = view.findViewById(R.id.contentLayout)
        contentLayout.isFocusableInTouchMode = true

        editText.requestFocus()

        editText.setOnKeyListener(onKey)
        contentLayout.setOnKeyListener(onKey)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManagerHelper.getLayoutFlag(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.dimAmount = 0.5f
        params.gravity = Gravity.TOP or Gravity.START

        OverlayService.instance.windowManager.addView(view, params)
    }

    fun addChatItem(title: String, text: String) {
        val chatItem = TextView(OverlayService.instance)
        chatItem.text = "$title: $text"
        chatItem.setTextColor(Color.WHITE)
        chat.addView(chatItem)
    }

    fun show() {
        view.visibility = View.VISIBLE
        editText.requestFocus()
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND

        OverlayService.instance.windowManager.updateViewLayout(view, params)
    }

    fun hide() {
        view.visibility = View.GONE
        params.flags = params.flags and (WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv())
        OverlayService.instance.windowManager.updateViewLayout(view, params)
    }
}

