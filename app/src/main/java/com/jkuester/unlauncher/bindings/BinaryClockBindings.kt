package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.getColorPaint
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockBinaryBinding
import java.util.Calendar

class BinaryClockState(context: Context) {
    val offPaint: Paint = getColorPaint(context, R.attr.colorAccent).apply {
        style = Paint.Style.STROKE
    }
    val onPaint: Paint = getColorPaint(context, R.attr.colorAccent).apply {
        style = Paint.Style.FILL_AND_STROKE
    }
    var centerPoint: Pair<Float, Float> = Pair(0F, 0F)
    var bitSize: Float = 20F
    var distance: Float = 10F
    val hourBounds: RectF = RectF(0F, 0F, 0F, 0F)
    val minuteBounds: RectF = RectF(0F, 0F, 0F, 0F)
    var is24Hour: Boolean = false
}

fun setupBinaryClockDateClickListener(fragment: Fragment) = { binding: ClockBinaryBinding ->
    binding.binaryDate.setOnClickListener(launchShowCalendar(fragment))
}

fun updateBinaryClockDate(resources: Resources, binding: ClockBinaryBinding): () -> Unit = {
    binding.binaryDate.text = getCurrentDateString(resources)
}

private fun getIs24HourFormat(context: Context, timeFormat: TimeFormat): Boolean = when (timeFormat) {
    TimeFormat.twenty_four_hour -> true
    TimeFormat.twelve_hour -> false
    else -> DateFormat.is24HourFormat(context)
}

fun observeIs24HourFormatChanges(
    context: Context,
    corePrefsRepo: DataRepository<CorePreferences>,
    state: BinaryClockState,
    onFormatChanged: () -> Unit
) {
    var currentIs24Hour: Boolean? = null
    corePrefsRepo.observe { corePrefs ->
        val previousIs24Hour = currentIs24Hour
        currentIs24Hour = getIs24HourFormat(context, corePrefs.timeFormat)
        state.is24Hour = currentIs24Hour
        if (previousIs24Hour != null && previousIs24Hour != currentIs24Hour) {
            onFormatChanged()
        }
    }
}

fun updateStateOnSizeChanged(state: BinaryClockState, w: Int, h: Int, viewWidth: Int) {
    state.centerPoint = Pair((w / 2).toFloat(), (h / 2).toFloat())
    state.distance = (w / 50).toFloat()
    state.bitSize = state.distance * 2

    val bitWidth = state.distance + 2 * state.bitSize
    val bitHeight = state.distance * 3 + state.bitSize * 2
    val startX = -state.centerPoint.first + bitWidth * 3
    val startY = state.centerPoint.second - bitHeight * 2

    state.hourBounds.set(startX, startY, startX + viewWidth.toFloat(), startY + bitHeight)
    state.minuteBounds.set(startX, startY + bitHeight, startX + viewWidth.toFloat(), startY + bitHeight * 2)
}

fun calculateMinimumDimensions(state: BinaryClockState, view: View, suggestedMinimumWidth: Int): Pair<Int, Int> {
    val minWidth = view.paddingLeft + view.paddingRight + suggestedMinimumWidth +
        12 * state.bitSize.toInt() + 7 * state.distance.toInt()
    val minHeight = view.paddingBottom + view.paddingTop +
        4 * state.bitSize.toInt() + 5 * state.distance.toInt()
    return Pair(minWidth, minHeight)
}

fun drawBinaryClock(state: BinaryClockState, canvas: Canvas) {
    val calendar = Calendar.getInstance()
    var hour = calendar[if (state.is24Hour) Calendar.HOUR_OF_DAY else Calendar.HOUR]
    val minute = calendar[Calendar.MINUTE]
    val isAm = calendar[Calendar.AM_PM] == Calendar.AM
    if (hour == 0 && !isAm) hour = 12
    renderBits(state, canvas, state.hourBounds, if (state.is24Hour) 5 else 4, hour)
    renderBits(state, canvas, state.minuteBounds, 6, minute)
}

private fun renderBits(state: BinaryClockState, canvas: Canvas, bounds: RectF, nBits: Int, value: Int) {
    val cw = state.distance + 2 * state.bitSize
    val cpx = cw / 2 - state.bitSize
    val cpy = bounds.height() / 2 - state.bitSize
    var x = bounds.right - cpx - state.bitSize
    val y = bounds.bottom - cpy - state.bitSize

    var bit = nBits
    var leftover = value
    while (bit > 0) {
        canvas.drawCircle(x, y, state.bitSize, if ((leftover and 1) != 1) state.offPaint else state.onPaint)
        x -= cw
        bit--
        leftover = leftover.ushr(1)
    }
}
