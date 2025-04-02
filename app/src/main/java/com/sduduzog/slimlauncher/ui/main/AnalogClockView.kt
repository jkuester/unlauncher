package com.sduduzog.slimlauncher.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.jkuester.unlauncher.WithActivityLifecycle
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class AnalogClockView(context: Context, attrs: AttributeSet) : ClockView(context, attrs) {
    @Inject @WithActivityLifecycle
    lateinit var corePreferencesRepo: DataRepository<CorePreferences>

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
    private val tickWidthMin = 2F
    private val tickLengthMin = 1F - .05F

    private var tickCount = 12

    init {
        handPaint.strokeWidth = handWidthMinute
        handPaint.style = Paint.Style.STROKE
        handPaint.strokeCap = Paint.Cap.ROUND

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnalogClockView,
            0,
            0
        ).apply {
            try {
                radius = getDimension(R.styleable.AnalogClockView_radius, 200F)
                border = getFloat(R.styleable.AnalogClockView_rim, 0F)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val calendar = Calendar.getInstance()

        val hour = calendar[Calendar.HOUR] % 12
        val minute = calendar[Calendar.MINUTE]
        val minuteF = minute / 60F
        val hourF = (hour + minuteF) / 12F

        val cx = width / 2F
        val cy = height / 2F + marginTop / 2F

        handPaint.strokeWidth = border
        if (border > 2) {
            canvas.drawCircle(cx, cy, radius, handPaint)
        }

        handPaint.strokeWidth = tickWidth
        drawTicks(canvas, cx, cy)

        handPaint.strokeWidth = handWidthHour
        drawHand(canvas, cx, cy, radius * handLengthHour, hourF)

        handPaint.strokeWidth = handWidthMinute
        drawHand(canvas, cx, cy, radius * handLengthMinute, minuteF)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, cnt: Int, rad: Float, len: Float) {
        val rot = 360F / cnt
        canvas.save()
        for (i in 1..tickCount) {
            canvas.rotate(rot, cx, cy)
            canvas.drawLine(cx, cy - rad, cx, cy - (rad * len), handPaint)
        }
        canvas.restore()
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float) {
        if (tickCount > 12) {
            drawTicks(canvas, cx, cy, 12, radius, tickLength)
            handPaint.strokeWidth = tickWidthMin
            drawTicks(canvas, cx, cy, tickCount, radius, tickLengthMin)
        } else {
            drawTicks(canvas, cx, cy, tickCount, radius, tickLength)
        }
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, size: Float, angleF: Float) {
        val angle = 360F * angleF
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawLine(cx, cy, cx, cy - size, handPaint)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dim = max(
            min(suggestedMinimumWidth, suggestedMinimumHeight),
            2 * radius.toInt()
        ) + 4 * border.toInt()
        val minw: Int = dim + paddingLeft + paddingRight + marginStart + marginEnd
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 0)

        val minh: Int = dim + paddingBottom + paddingTop + marginTop + marginBottom
        val h: Int = resolveSizeAndState(minh, heightMeasureSpec, 0)

        setMeasuredDimension(w, h)
    }

    override fun updateClock(corePrefs: CorePreferences) {
        super.updateClock(corePrefs)
        val clockType = corePrefs.clockType
        tickCount = when (clockType) {
            ClockType.analog_0 -> 0
            ClockType.analog_1 -> 1
            ClockType.analog_2 -> 2
            ClockType.analog_3 -> 3
            ClockType.analog_4 -> 4
            ClockType.analog_6 -> 6
            ClockType.analog_12 -> 12
            ClockType.analog_60 -> 60
            else -> 12
        }
    }
}
