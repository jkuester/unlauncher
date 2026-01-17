package com.jkuester.unlauncher.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.view.Gravity
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.BinaryClockState
import com.jkuester.unlauncher.bindings.calculateMinimumDimensions
import com.jkuester.unlauncher.bindings.observeIs24HourFormatChanges
import com.jkuester.unlauncher.bindings.setupBinaryClockDateClickListener
import com.jkuester.unlauncher.bindings.updateBinaryClockDate
import com.jkuester.unlauncher.bindings.updateStateOnSizeChanged
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.jkuester.unlauncher.launchShowAlarms
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

    @Inject
    @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    private val state = BinaryClockState(context)

    private val binding: ClockBinaryBinding

    init {
        inflate(context, R.layout.clock_binary, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ClockBinaryBinding
            .bind(this)
            .also(setupBinaryClockDateClickListener(fragment))
        setWillNotDraw(false)

        setOnClickListener(launchShowAlarms(fragment))
        observeIs24HourFormatChanges(
            context,
            corePrefsRepo,
            onInitialValue = { state.is24Hour = it },
            onFormatChanged = {
                state.is24Hour = it
                invalidate()
            }
        )
        updateChildViews()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val calendar = Calendar.getInstance()

        var hour = calendar[if (state.is24Hour) Calendar.HOUR_OF_DAY else Calendar.HOUR]
        if (hour == 0 && calendar[Calendar.AM] != 0) hour = 12
        renderBits(canvas, state.hourBounds, if (state.is24Hour) 5 else 4, hour)

        val minute = calendar[Calendar.MINUTE]
        renderBits(canvas, state.minuteBounds, 6, minute)
    }

    private fun renderBits(canvas: Canvas, bounds: RectF, nBits: Int, value: Int) {
        val cw = state.distance + 2 * state.bitSize
        val ch = bounds.height()
        val cpx = cw / 2 - state.bitSize
        val cpy = ch / 2 - state.bitSize
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val (minw, minh) = calculateMinimumDimensions(state, this, suggestedMinimumWidth)
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 0)
        val h: Int = resolveSizeAndState(minh, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }

    override fun invalidate() {
        super.invalidate()
        updateChildViews()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateStateOnSizeChanged(state, w, h, width)
    }

    private fun updateChildViews() {
        updateBinaryClockDate(resources)(binding)
    }
}
