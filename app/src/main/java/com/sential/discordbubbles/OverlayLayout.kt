package com.sential.discordbubbles

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView


class OverlayLayout : LinearLayout {
    lateinit var params: WindowManager.LayoutParams

    lateinit var view: View
    lateinit var darkBackground: LinearLayout
    lateinit var editText: EditText
    lateinit var contentLayout: LinearLayout
    lateinit var chat: LinearLayout

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    fun addChatItem(title: String, text: String) {
        val chatItem = TextView(context)
        chatItem.text = "$title: $text"
        chatItem.setTextColor(Color.WHITE)
        chat.addView(chatItem)
    }

    private fun init() {
        view = LayoutInflater.from(context).inflate(R.layout.chat, null)
        view.visibility = View.GONE

        editText = view.findViewById(R.id.editText)

        darkBackground = view.findViewById(R.id.darkBg)
        darkBackground.setOnClickListener {
            OverlayService.instance.hide()
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
            OverlayService.getLayoutFlag(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.dimAmount = 0.5f

        //params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

        params.gravity = Gravity.TOP or Gravity.START
    }

    private val onKey: View.OnKeyListener = View.OnKeyListener { _, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_MENU) {
            OverlayService.instance.hide()
            return@OnKeyListener true
        }
        return@OnKeyListener false
    }

    fun show() {
        view.visibility = View.VISIBLE
        editText.requestFocus()
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND

        OverlayService.instance.windowManager.updateViewLayout(view, params)
        //darkBackground.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    fun hide() {
        view.visibility = View.GONE
        params.flags = 0
        OverlayService.instance.windowManager.updateViewLayout(view, params)
    }
}

