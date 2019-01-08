package com.sential.discordbubbles

import android.graphics.*
import android.graphics.Bitmap
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.BlurMaskFilter

class ImageHelper {
    companion object {
        fun getCircularBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)

            paint.color = -0xbdbdbe
            canvas.drawCircle(output.width.toFloat() / 2, output.height.toFloat() / 2, output.width.toFloat() / 2, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return Bitmap.createScaledBitmap(output, ChatHeadsArrangement.CHAT_HEAD_SIZE, ChatHeadsArrangement.CHAT_HEAD_SIZE, true);
        }

        fun addShadow(src: Bitmap): Bitmap {
            val bmOut = Bitmap.createBitmap(src.width + 10, src.height + 10, Bitmap.Config.ARGB_8888)

            val centerX = (bmOut.width / 2 - src.width / 2).toFloat()
            val centerY = (bmOut.height / 2 - src.height / 2).toFloat()

            val canvas = Canvas(bmOut)
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            val ptBlur = Paint()
            ptBlur.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
            val offsetXY = IntArray(2)
            val bmAlpha = src.extractAlpha(ptBlur, offsetXY)
            val ptAlphaColor = Paint()
            ptAlphaColor.color = Color.argb(70, 0, 0, 0)
            canvas.drawBitmap(bmAlpha, centerX + offsetXY[0], centerY  + offsetXY[1], ptAlphaColor)
            bmAlpha.recycle()
            canvas.drawBitmap(src, centerX, centerY,null)
            return bmOut
        }
    }
}