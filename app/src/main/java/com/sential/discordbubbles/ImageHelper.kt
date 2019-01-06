package com.sential.discordbubbles

import android.graphics.*
import android.graphics.Bitmap
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.BlurMaskFilter

class ImageHelper {
    companion object {
        fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
            val output = Bitmap.createBitmap(
                bitmap.width, bitmap
                    .height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)
            val roundPx = pixels.toFloat()

            paint.setAntiAlias(true)
            canvas.drawARGB(0, 0, 0, 0)
            paint.setColor(color)
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

        fun addShadow(src: Bitmap): Bitmap {
            val bmOut = Bitmap.createBitmap(src.getWidth() + 400, src.getHeight() + 400, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmOut)
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            val ptBlur = Paint()
            ptBlur.maskFilter = BlurMaskFilter(150f, BlurMaskFilter.Blur.NORMAL)
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