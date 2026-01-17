package com.jkuester.unlauncher.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.AnalogClockState
import com.jkuester.unlauncher.bindings.ViewDimensions
import com.jkuester.unlauncher.bindings.calculateMinimumDimensions
import com.jkuester.unlauncher.bindings.drawAnalogClock
import com.jkuester.unlauncher.bindings.observeAnalogClockTypeChanges
import com.jkuester.unlauncher.bindings.setupAnalogClockDateClickListener
import com.jkuester.unlauncher.bindings.updateAnalogClockDate
import com.jkuester.unlauncher.bindings.updateStateOnSizeChanged
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.jkuester.unlauncher.launchShowAlarms
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockAnalogBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import javax.inject.Inject

@AndroidEntryPoint
@WithFragmentBindings
class AnalogClockView(context: Context) : LinearLayout(context) {
    @Inject
    lateinit var fragment: Fragment

    @Inject
    @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    private val state = AnalogClockState(context)

    private val binding: ClockAnalogBinding
    private val updateChildViews: () -> Unit

    init {
        inflate(context, R.layout.clock_analog, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ClockAnalogBinding
            .bind(this)
            .also(setupAnalogClockDateClickListener(fragment))
        setWillNotDraw(false)
        setOnClickListener(launchShowAlarms(fragment))
        updateChildViews = updateAnalogClockDate(resources, binding)
        observeAnalogClockTypeChanges(corePrefsRepo, state, ::invalidate)
        updateChildViews()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAnalogClock(state, canvas, width, height, marginTop)
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val (minw, minh) = calculateMinimumDimensions(
            state,
            ViewDimensions(this),
            suggestedMinimumWidth,
            suggestedMinimumHeight
        )
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 0)
        val h: Int = resolveSizeAndState(minh, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateStateOnSizeChanged(state, width)
    }

    override fun invalidate() {
        super.invalidate()
        updateChildViews()
    }
}
