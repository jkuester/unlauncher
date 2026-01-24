package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.AnalogClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.getColorPaint
import com.jkuester.unlauncher.getCurrentDateString
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockAnalogBinding
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

data class ViewDimensions(
    val paddingLeft: Int,
    val paddingRight: Int,
    val paddingTop: Int,
    val paddingBottom: Int,
    val marginStart: Int,
    val marginEnd: Int,
    val marginTop: Int,
    val marginBottom: Int
) {
    constructor(view: View) : this(
        paddingLeft = view.paddingLeft,
        paddingRight = view.paddingRight,
        paddingTop = view.paddingTop,
        paddingBottom = view.paddingBottom,
        marginStart = view.marginStart,
        marginEnd = view.marginEnd,
        marginTop = view.marginTop,
        marginBottom = view.marginBottom
    )
}

class AnalogClockState(context: Context) {
    val handPaint: Paint = getColorPaint(context, R.attr.colorAccent).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    var radius = 0F
    var border = 0F
    var tickCount = 0

    // Length is given in fraction of radius, width is in pixels
    val handWidthHour = 10F
    val handWidthMinute = 5F
    val handLengthHour = .6F
    val handLengthMinute = .8F

    val tickWidth = 4F
    val tickLength = 1F - .1F
    val tickWidthMin = 2F
    val tickLengthMin = 1F - .05F
}

fun updateAnalogClockDate(resources: Resources, binding: ClockAnalogBinding): () -> Unit = {
    binding.analogDate.text = getCurrentDateString(resources)
}

private fun getTickCount(analogClockType: AnalogClockType) = when (analogClockType) {
    AnalogClockType.analog_0 -> 0
    AnalogClockType.analog_1 -> 1
    AnalogClockType.analog_2 -> 2
    AnalogClockType.analog_3 -> 3
    AnalogClockType.analog_4 -> 4
    AnalogClockType.analog_6 -> 6
    AnalogClockType.analog_12 -> 12
    AnalogClockType.analog_60 -> 60
    else -> 12
}

fun observeAnalogClockTypeChanges(
    corePrefsRepo: DataRepository<CorePreferences>,
    state: AnalogClockState,
    onTickCountChanged: () -> Unit
) {
    var currentTickCount: Int? = null
    corePrefsRepo.observe { corePrefs ->
        val previousTickCount = currentTickCount
        currentTickCount = getTickCount(corePrefs.analogClockType)
        state.tickCount = currentTickCount!!
        if (previousTickCount != null && previousTickCount != currentTickCount) {
            onTickCountChanged()
        }
    }
}

fun updateStateOnSizeChanged(state: AnalogClockState, viewWidth: Int) {
    state.radius = (viewWidth * 0.75F) / 2F
}

fun calculateMinimumDimensions(
    state: AnalogClockState,
    dimensions: ViewDimensions,
    suggestedMinimumWidth: Int,
    suggestedMinimumHeight: Int
): Pair<Int, Int> {
    val dim = max(
        min(suggestedMinimumWidth, suggestedMinimumHeight),
        2 * state.radius.toInt()
    ) + 4 * state.border.toInt()
    val minWidth = dim + dimensions.paddingLeft + dimensions.paddingRight +
        dimensions.marginStart + dimensions.marginEnd
    val minHeight = dim + dimensions.paddingBottom + dimensions.paddingTop +
        dimensions.marginTop + dimensions.marginBottom
    return Pair(minWidth, minHeight)
}

fun drawAnalogClock(state: AnalogClockState, canvas: Canvas, width: Int, height: Int, marginTop: Int) {
    val calendar = Calendar.getInstance()

    val hour = calendar[Calendar.HOUR] % 12
    val minute = calendar[Calendar.MINUTE]
    val minuteF = minute / 60F
    val hourF = (hour + minuteF) / 12F

    val cx = width / 2F
    val cy = height / 2F + marginTop / 2F

    state.handPaint.strokeWidth = state.border
    if (state.border > 2) {
        canvas.drawCircle(cx, cy, state.radius, state.handPaint)
    }

    state.handPaint.strokeWidth = state.tickWidth
    drawTicks(state, canvas, cx, cy)

    state.handPaint.strokeWidth = state.handWidthHour
    drawHand(state, canvas, cx, cy, state.radius * state.handLengthHour, hourF)

    state.handPaint.strokeWidth = state.handWidthMinute
    drawHand(state, canvas, cx, cy, state.radius * state.handLengthMinute, minuteF)
}

private fun drawTicks(state: AnalogClockState, canvas: Canvas, cx: Float, cy: Float, cnt: Int, rad: Float, len: Float) {
    val rot = 360F / cnt
    canvas.withSave {
        for (i in 1..state.tickCount) {
            rotate(rot, cx, cy)
            drawLine(cx, cy - rad, cx, cy - (rad * len), state.handPaint)
        }
    }
}

private fun drawTicks(state: AnalogClockState, canvas: Canvas, cx: Float, cy: Float) {
    if (state.tickCount > 12) {
        drawTicks(state, canvas, cx, cy, 12, state.radius, state.tickLength)
        state.handPaint.strokeWidth = state.tickWidthMin
        drawTicks(state, canvas, cx, cy, state.tickCount, state.radius, state.tickLengthMin)
    } else {
        drawTicks(state, canvas, cx, cy, state.tickCount, state.radius, state.tickLength)
    }
}

private fun drawHand(state: AnalogClockState, canvas: Canvas, cx: Float, cy: Float, size: Float, angleF: Float) {
    val angle = 360F * angleF
    canvas.withRotation(angle, cx, cy) {
        drawLine(cx, cy, cx, cy - size, state.handPaint)
    }
}
