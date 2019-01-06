package com.sential.discordbubbles

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout

class OverlayLayout : LinearLayout {
    lateinit var params: WindowManager.LayoutParams

    lateinit var view: View
    lateinit var darkBackground: LinearLayout
    lateinit var editText: EditText

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

        darkBackground = view.findViewById(R.id.darkBg)
        darkBackground.setOnClickListener {
            OverlayService.instance.hide()
        }

        editText = view.findViewById(R.id.editText)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            OverlayService.getLayoutFlag(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        val metrics = OverlayService.getScreenSize()

        params.gravity = Gravity.TOP or Gravity.START
    }

    fun hide() {
        view.visibility = View.GONE
    }
}