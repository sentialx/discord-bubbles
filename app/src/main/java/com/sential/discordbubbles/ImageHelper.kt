package com.sential.discordbubbles

import android.graphics.*
import android.graphics.Bitmap
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.BlurMaskFilter

class ImageHelper {
    companion object {
        fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Float): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)

            paint.color = -0xbdbdbe
            canvas.drawRoundRect(RectF(rect), pixels, pixels, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

        fun addShadow(src: Bitmap): Bitmap {
            val bmOut = Bitmap.createBitmap(src.width + 400, src.height + 400, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmOut)
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            val ptBlur = Paint()
            ptBlur.maskFilter = BlurMaskFilter(75f, BlurMaskFilter.Blur.NORMAL)
            val offsetXY = IntArray(2)
            val bmAlpha = src.extractAlpha(ptBlur, offsetXY)
            val ptAlphaColor = Paint()
            ptAlphaColor.color = Color.argb(70, 0, 0, 0)
            canvas.drawBitmap(bmAlpha, offsetXY[0].toFloat() + 200f, offsetXY[1].toFloat() + 200f, ptAlphaColor)
            bmAlpha.recycle()
            canvas.drawBitmap(src, 200f, 200f, null)
            return bmOut
        }
    }
}