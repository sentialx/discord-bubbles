package com.sential.discordbubbles

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout


class OverlayLayout : LinearLayout {
    lateinit var params: WindowManager.LayoutParams

    lateinit var view: View
    lateinit var darkBackground: LinearLayout
    lateinit var editText: EditText
    lateinit var contentLayout: RelativeLayout

    constructor(context: Context) : super(context) {
        init();
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init();
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init();
    }

    private fun init() {
        view = LayoutInflater.from(context).inflate(R.layout.chat, null)
        view.visibility = View.GONE

        editText = view.findViewById(R.id.editText)

        darkBackground = view.findViewById(R.id.darkBg)
        darkBackground.setOnClickListener {
            OverlayService.instance.hide()
        }

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

        //darkBackground.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    fun hide() {
        view.visibility = View.GONE
    }
}

