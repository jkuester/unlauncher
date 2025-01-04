package com.sduduzog.slimlauncher.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

abstract class ClockView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private fun getColor(constant: Int): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(constant, tv, true)
        return tv.data
    }

    fun getColorPaint(constant: Int): Paint {
        val paint = Paint()
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = getColor(constant)
        return paint
    }

    open fun updateClock() {
        requestLayout()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) = super.onDraw(canvas)

    abstract override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
}
