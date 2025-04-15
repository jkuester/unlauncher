package com.jkuester.unlauncher.dialog

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class ThemeColorPreviewDrawable(
    val sizePx: Int,
    val strokeWidthPx: Float,
    @ColorInt val bgColor: Int,
    @ColorInt val headerColor: Int,
    @ColorInt val fontColor: Int,
    @ColorInt val strokeColor: Int
) : Drawable() {

    private val paint: Paint = Paint()
    private val spacingPx: Int = (sizePx * 0.1f).toInt()

    override fun draw(canvas: Canvas) {
        val radius = sizePx / 2.toFloat()
        drawCircleWithStroke(radius, radius, radius, bgColor, strokeColor, canvas)
        drawCircleWithStroke(radius * 3 + spacingPx, radius, radius, headerColor, strokeColor, canvas)
        drawCircleWithStroke(radius * 5 + spacingPx * 2, radius, radius, fontColor, strokeColor, canvas)
    }

    private fun drawCircleWithStroke(
        cx: Float,
        cy: Float,
        radius: Float,
        @ColorInt fillColor: Int,
        @ColorInt strokeColor: Int,
        canvas: Canvas
    ) {
        paint.style = Paint.Style.FILL
        paint.color = fillColor
        canvas.drawCircle(cx, cy, radius - strokeWidthPx / 2, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidthPx
        paint.color = strokeColor
        canvas.drawCircle(cx, cy, radius - strokeWidthPx / 2, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(filter: ColorFilter?) {
        paint.colorFilter = filter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicHeight(): Int = sizePx

    override fun getIntrinsicWidth(): Int = 3 * sizePx + 2 * spacingPx
}
