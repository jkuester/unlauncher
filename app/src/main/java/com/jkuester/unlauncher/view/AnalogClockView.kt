package com.jkuester.unlauncher.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.AnalogClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.jkuester.unlauncher.getColorPaint
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowAlarms
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockAnalogBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
@WithFragmentBindings
class AnalogClockView(context: Context) : LinearLayout(context) {
    @Inject
    lateinit var fragment: Fragment
    @Inject @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    private var handPaint = getColorPaint(context, R.attr.colorAccent)
    private var radius = 0F
    private var border = 0F

    // Length is given in fraction of radius, width is in pixels
    private val handWidthHour = 10F
    private val handWidthMinute = 5F
    private val handLengthHour = .6F
    private val handLengthMinute = .8F

    private val tickWidth = 4F
    private val tickLength = 1F - .1F
    private val tickWidthMin = 2F
    private val tickLengthMin = 1F - .05F

    private val binding: ClockAnalogBinding
    private var tickCount = 0

    init {
        inflate(context, R.layout.clock_analog, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ClockAnalogBinding.bind(this)
        setWillNotDraw(false)

        corePrefsRepo.observe(this::listenForChangesToClockType)

        handPaint.strokeWidth = handWidthMinute
        handPaint.style = Paint.Style.STROKE
        handPaint.strokeCap = Paint.Cap.ROUND

        setOnClickListener(launchShowAlarms(fragment))
        binding.analogDate.setOnClickListener(launchShowCalendar(fragment))
        updateChildViews()
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
        canvas.withSave {
            for (i in 1..tickCount) {
                rotate(rot, cx, cy)
                drawLine(cx, cy - rad, cx, cy - (rad * len), handPaint)
            }
        }
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
        canvas.withRotation(angle, cx, cy) {
            drawLine(cx, cy, cx, cy - size, handPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = (width * 0.75F) / 2F
    }

    override fun invalidate() {
        super.invalidate()
        updateChildViews()
    }

    private fun updateChildViews() {
        binding.analogDate.text = getCurrentDateString(resources)
    }

    private fun listenForChangesToClockType(corePrefs: CorePreferences) {
        val originalTickCount = tickCount
        tickCount = getTickCount(corePrefs.analogClockType)
        if (originalTickCount != tickCount) {
            invalidate()
        }
    }
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
