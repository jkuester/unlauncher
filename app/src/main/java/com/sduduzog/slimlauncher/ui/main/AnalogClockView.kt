package com.sduduzog.slimlauncher.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.sduduzog.slimlauncher.R
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

class AnalogClockView(context: Context, attrs: AttributeSet) : ClockView(context, attrs) {
    private var handPaint = getColorPaint(R.attr.colorAccent)
    private var radius: Float
    private var border: Float

    // Length is given in fraction of radius, width is in pixels
    private val handWidthHour = 10F
    private val handWidthMinute = 5F
    private val handLengthHour = .6F
    private val handLengthMinute = .8F
    private val tickWidth = 4F
    private val tickLength = 1F - .1F

    init {
        handPaint.strokeWidth = handWidthMinute
        handPaint.style = Paint.Style.STROKE
        handPaint.strokeCap = Paint.Cap.ROUND

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnalogClockView,
            0, 0).apply {
            try {
                radius = getDimension(R.styleable.AnalogClockView_radius, 200F)
                border = getFloat(R.styleable.AnalogClockView_rim, 0F)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas : Canvas)
    {
        super.onDraw(canvas)
        val calendar = Calendar.getInstance()

        val hour = calendar[Calendar.HOUR]
        val minute = calendar[Calendar.MINUTE]

        val cx = width / 2F
        val cy = height / 2F + marginTop / 2F

        handPaint.strokeWidth = border
        if (border > 2) canvas.drawCircle(cx, cy, radius, handPaint)
        handPaint.strokeWidth = tickWidth
        drawTicks(canvas, cx, cy)
        handPaint.strokeWidth = handWidthHour
        drawHand(canvas, cx, cy, radius * handLengthHour, hour * 5)
        handPaint.strokeWidth = handWidthMinute
        drawHand(canvas, cx, cy, radius * handLengthMinute, minute)
    }

    private fun drawTicks(canvas: Canvas, cx : Float, cy : Float) {
        canvas.save()
        for (i in 1..12) {
            canvas.rotate(30f, cx, cy)
            canvas.drawLine(cx, cy + radius, cx, cy + (radius * tickLength), handPaint)
        }
        canvas.restore()
    }

    private fun drawHand(canvas: Canvas, cx : Float, cy : Float, size : Float, minute : Int) {
        val angle : Float = (minute.toFloat() * 6)

        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawLine(cx, cy, cx, cy - size, handPaint)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dim = max(min(suggestedMinimumWidth, suggestedMinimumHeight),
            2 * radius.toInt()) + 4 * border.toInt()
        val minw: Int = dim + paddingLeft + paddingRight + marginStart + marginEnd
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 0)

        val minh: Int = dim + paddingBottom + paddingTop + marginTop + marginBottom
        val h: Int = resolveSizeAndState(minh, heightMeasureSpec, 0)

        setMeasuredDimension(w, h)
    }

}