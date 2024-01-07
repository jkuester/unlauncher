package com.sduduzog.slimlauncher.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.sduduzog.slimlauncher.R
import java.util.Calendar

class BinaryClockView(context: Context, attrs: AttributeSet) :
    ClockView(context, attrs) {

    private var offPaint = getColorPaint(R.attr.colorAccent)
    private var onPaint = getColorPaint(R.attr.colorAccent)
    private var bitSize: Float
    private var border: Float
    private var distance: Float
    private val bounds = RectF(0F, 0F, 0F, 0F)
    private var is24Hour: Boolean = false

    init {
        onPaint.style = Paint.Style.FILL_AND_STROKE
        offPaint.style = Paint.Style.STROKE
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BinaryClockView,
            0,
            0
        ).apply {
            try {
                bitSize = getFloat(R.styleable.BinaryClockView_bitSize, 40F)
                border = getFloat(R.styleable.BinaryClockView_border, 4F)
                distance = getFloat(R.styleable.BinaryClockView_distance, 0F)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val calendar = Calendar.getInstance()

        val middle = if (distance > 0) distance * 3 + bitSize * 2 else height.toFloat() / 2
        var hour = calendar[if (is24Hour) Calendar.HOUR_OF_DAY else Calendar.HOUR]
        if (hour == 0 && calendar[Calendar.AM] != 0) hour = 12
        bounds.set(0f, 0f, width.toFloat(), middle)
        renderBits(canvas, bounds, if (is24Hour) 5 else 4, hour)

        bounds.set(0f, middle, width.toFloat(), middle * 2)
        val minute = calendar[Calendar.MINUTE]
        renderBits(canvas, bounds, 6, minute)
    }

    private fun renderBits(canvas: Canvas, bounds: RectF, nBits: Int, value: Int) {
        val cw = if (distance > 0) {
            distance + 2 * bitSize
        } else {
            bounds.width() / 18 // divide width by maximal number of bits * 3
        }
        val ch = bounds.height()
        val cpx = cw / 2 - bitSize
        val cpy = ch / 2 - bitSize
        var x = bounds.right - cpx - bitSize
        val y = bounds.bottom - cpy - bitSize

        var bit = nBits
        var leftover = value
        while (bit > 0) {
            canvas.drawCircle(x, y, bitSize, if ((leftover and 1) != 1) offPaint else onPaint)
            x -= cw
            bit--
            leftover = leftover.ushr(1)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try for a width based on your minimum.
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth +
            12 * bitSize.toInt() + 7 * distance.toInt()
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 0)

        // Whatever the width is, ask for a height that lets the pie get as big as
        // it can.
        val minh: Int = paddingBottom + paddingTop +
            4 * bitSize.toInt() + 5 * distance.toInt()
        val h: Int = resolveSizeAndState(minh, heightMeasureSpec, 0)

        setMeasuredDimension(w, h)
    }
}
