package com.jkuester.unlauncher.view

import android.content.Context
import android.graphics.Canvas
import android.view.Gravity
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.BinaryClockState
import com.jkuester.unlauncher.bindings.calculateMinimumDimensions
import com.jkuester.unlauncher.bindings.drawBinaryClock
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
        observeIs24HourFormatChanges(context, corePrefsRepo, state, ::invalidate)
        updateChildViews()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBinaryClock(state, canvas)
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
