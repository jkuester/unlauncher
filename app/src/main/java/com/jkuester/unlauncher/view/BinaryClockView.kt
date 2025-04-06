package com.jkuester.unlauncher.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.format.DateFormat
import android.view.Gravity
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.jkuester.unlauncher.getColorPaint
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowAlarms
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockBinaryBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
@WithFragmentBindings
class BinaryClockView(context: Context) : LinearLayout(context) {
    @Inject
    lateinit var fragment: Fragment
    @Inject @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    private var offPaint = getColorPaint(context, R.attr.colorAccent)
    private var onPaint = getColorPaint(context, R.attr.colorAccent)

    private var centerPoint = Pair(0F, 0F)
    private var bitSize = 20F
    private var distance = 10F

    private val hourBounds = RectF(0F, 0F, 0F, 0F)
    private val minuteBounds = RectF(0F, 0F, 0F, 0F)
    private var is24Hour: Boolean = false

    private val binding: ClockBinaryBinding

    init {
        inflate(context, R.layout.clock_binary, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ClockBinaryBinding.bind(this)
        setWillNotDraw(false)

        corePrefsRepo.observe(this::listenForChangesToTimeFormat)

        onPaint.style = Paint.Style.FILL_AND_STROKE
        offPaint.style = Paint.Style.STROKE

        setOnClickListener(launchShowAlarms(fragment))
        binding.binaryDate.setOnClickListener(launchShowCalendar(fragment))
        updateChildViews()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val calendar = Calendar.getInstance()

        var hour = calendar[if (is24Hour) Calendar.HOUR_OF_DAY else Calendar.HOUR]
        if (hour == 0 && calendar[Calendar.AM] != 0) hour = 12
        renderBits(canvas, hourBounds, if (is24Hour) 5 else 4, hour)

        val minute = calendar[Calendar.MINUTE]
        renderBits(canvas, minuteBounds, 6, minute)
    }

    private fun renderBits(canvas: Canvas, bounds: RectF, nBits: Int, value: Int) {
        val cw = distance + 2 * bitSize
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
        measureChildren(widthMeasureSpec, heightMeasureSpec)
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

    override fun invalidate() {
        super.invalidate()
        updateChildViews()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerPoint = Pair((w / 2).toFloat(), (h / 2).toFloat())
        distance = (w / 50).toFloat()
        bitSize = distance * 2

        val bitWidth = distance + 2 * bitSize
        val bitHeight = distance * 3 + bitSize * 2
        val startX = -centerPoint.first + bitWidth * 3
        val startY = centerPoint.second - bitHeight * 2

        hourBounds.set(startX, startY, startX + width.toFloat(), startY + bitHeight)
        minuteBounds.set(startX, startY + bitHeight, startX + width.toFloat(), startY + bitHeight * 2)
    }

    private fun updateChildViews() {
        binding.binaryDate.text = getCurrentDateString(resources)
    }

    private fun listenForChangesToTimeFormat(corePrefs: CorePreferences) {
        val originalIs24Hour = is24Hour
        is24Hour = when (corePrefs.timeFormat) {
            TimeFormat.twenty_four_hour -> true
            TimeFormat.twelve_hour -> false
            else -> DateFormat.is24HourFormat(context)
        }

        if (originalIs24Hour != is24Hour) {
            invalidate()
        }
    }
}
