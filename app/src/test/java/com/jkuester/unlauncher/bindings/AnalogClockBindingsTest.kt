package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datastore.proto.AnalogClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.getColorPaint
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowCalendar
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockAnalogBinding
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.util.Calendar
import kotlin.reflect.KFunction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class AnalogClockBindingsTest {
    @MockK
    lateinit var context: Context
    @MockK
    lateinit var resources: Resources
    @MockK
    lateinit var fragment: Fragment
    @MockK
    lateinit var rootView: LinearLayout
    @MockK
    lateinit var analogDate: TextView

    private lateinit var binding: ClockAnalogBinding

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.analog_date) } returns analogDate

        binding = ClockAnalogBinding.bind(rootView)
    }

    @Test
    fun setupAnalogClockDateClickListener_setsDateClickListener() {
        mockkStatic(::launchShowCalendar)
        val dateClickListener = mockk<OnClickListener>()
        every { launchShowCalendar(fragment) } returns dateClickListener
        justRun { analogDate.setOnClickListener(any()) }

        setupAnalogClockDateClickListener(fragment)(binding)

        verify(exactly = 1) { launchShowCalendar(fragment) }
        verify(exactly = 1) { analogDate.setOnClickListener(dateClickListener) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.analog_date) }
    }

    @Test
    fun updateAnalogClockDate_setsDateText() {
        mockkStatic(::getCurrentDateString)
        every { getCurrentDateString(resources) } returns "Fri, Jan 02"
        justRun { analogDate.text = any() }

        updateAnalogClockDate(resources, binding)()

        verify(exactly = 1) { analogDate.text = "Fri, Jan 02" }
        verify(exactly = 1) { getCurrentDateString(resources) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.analog_date) }
    }

    @Test
    fun analogClockState_initializesWithDefaultValues() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val state = AnalogClockState(context)

        state.radius shouldBe 0F
        state.border shouldBe 0F
        state.tickCount shouldBe 0
        state.handWidthHour shouldBe 10F
        state.handWidthMinute shouldBe 5F
        state.handLengthHour shouldBe .6F
        state.handLengthMinute shouldBe .8F
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun observeAnalogClockTypeChanges_withAnalog0_setsStateTo0() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_0).build()
        )
        val state = AnalogClockState(context)
        var tickCountChangedCount = 0

        observeAnalogClockTypeChanges(corePrefsRepo, state) { tickCountChangedCount++ }

        state.tickCount shouldBe 0
        tickCountChangedCount shouldBe 0
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun observeAnalogClockTypeChanges_withAnalog12_setsStateTo12() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        )
        val state = AnalogClockState(context)
        var tickCountChangedCount = 0

        observeAnalogClockTypeChanges(corePrefsRepo, state) { tickCountChangedCount++ }

        state.tickCount shouldBe 12
        tickCountChangedCount shouldBe 0
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun observeAnalogClockTypeChanges_withAnalog60_setsStateTo60() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_60).build()
        )
        val state = AnalogClockState(context)
        var tickCountChangedCount = 0

        observeAnalogClockTypeChanges(corePrefsRepo, state) { tickCountChangedCount++ }

        state.tickCount shouldBe 60
        tickCountChangedCount shouldBe 0
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun observeAnalogClockTypeChanges_callsOnTickCountChangedWhenTypeChanges() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        )
        val state = AnalogClockState(context)
        var tickCountChangedCount = 0

        observeAnalogClockTypeChanges(corePrefsRepo, state) { tickCountChangedCount++ }

        state.tickCount shouldBe 12
        tickCountChangedCount shouldBe 0

        corePrefsRepo.updateAsync {
            it.toBuilder().setAnalogClockType(AnalogClockType.analog_60).build()
        }
        state.tickCount shouldBe 60
        tickCountChangedCount shouldBe 1

        corePrefsRepo.updateAsync {
            it.toBuilder().setAnalogClockType(AnalogClockType.analog_0).build()
        }
        state.tickCount shouldBe 0
        tickCountChangedCount shouldBe 2
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun observeAnalogClockTypeChanges_doesNotCallOnTickCountChangedWhenUnchanged() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        )
        val state = AnalogClockState(context)
        var tickCountChangedCount = 0

        observeAnalogClockTypeChanges(corePrefsRepo, state) { tickCountChangedCount++ }

        tickCountChangedCount shouldBe 0

        corePrefsRepo.updateAsync {
            it.toBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        }
        tickCountChangedCount shouldBe 0
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun observeAnalogClockTypeChanges_allAnalogClockTypesMapCorrectly() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val expectedMappings = mapOf(
            AnalogClockType.analog_0 to 0,
            AnalogClockType.analog_1 to 1,
            AnalogClockType.analog_2 to 2,
            AnalogClockType.analog_3 to 3,
            AnalogClockType.analog_4 to 4,
            AnalogClockType.analog_6 to 6,
            AnalogClockType.analog_12 to 12,
            AnalogClockType.analog_60 to 60
        )

        expectedMappings.forEach { (clockType, expectedTickCount) ->
            val corePrefsRepo = TestDataRepository(
                CorePreferences.newBuilder().setAnalogClockType(clockType).build()
            )
            val state = AnalogClockState(context)

            observeAnalogClockTypeChanges(corePrefsRepo, state) { }

            state.tickCount shouldBe expectedTickCount
        }
        verify(exactly = 8) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 8) { paint.style = Paint.Style.STROKE }
        verify(exactly = 8) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun updateStateOnSizeChanged_updatesRadius() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val state = AnalogClockState(context)

        updateStateOnSizeChanged(state, 400)

        state.radius shouldBe 150F // (400 * 0.75) / 2
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun calculateMinimumDimensions_returnsCorrectDimensions() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }

        val state = AnalogClockState(context)
        state.radius = 100F
        state.border = 5F

        val dimensions = ViewDimensions(
            paddingLeft = 10,
            paddingRight = 10,
            paddingTop = 5,
            paddingBottom = 5,
            marginStart = 2,
            marginEnd = 2,
            marginTop = 3,
            marginBottom = 3
        )

        val (minWidth, minHeight) = calculateMinimumDimensions(
            state,
            dimensions,
            suggestedMinimumWidth = 50,
            suggestedMinimumHeight = 50
        )

        // dim = max(min(50, 50), 2*100) + 4*5 = max(50, 200) + 20 = 220
        // minWidth = 220 + 10 + 10 + 2 + 2 = 244
        // minHeight = 220 + 5 + 5 + 3 + 3 = 236
        minWidth shouldBe 244
        minHeight shouldBe 236
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
    }

    @Test
    fun drawAnalogClock_drawsClockElements() {
        val paint = mockk<Paint>()
        val canvas = mockk<Canvas>()
        val calendar = mockk<Calendar>()
        mockkStatic(::getColorPaint)
        mockkStatic(Calendar::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { Calendar.getInstance() } returns calendar
        every { calendar[Calendar.HOUR] } returns 10
        every { calendar[Calendar.MINUTE] } returns 30
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }
        justRun { paint.strokeWidth = any() }
        justRun { canvas.drawLine(any(), any(), any(), any(), any()) }
        every { canvas.save() } returns 0
        justRun { canvas.rotate(any(), any(), any()) }
        justRun { canvas.restoreToCount(any()) }

        val state = AnalogClockState(context)
        state.radius = 100F
        state.tickCount = 12

        drawAnalogClock(state, canvas, 200, 200, 0)

        verify(exactly = 1) { Calendar.getInstance() }
        verify(exactly = 1) { calendar[Calendar.HOUR] }
        verify(exactly = 1) { calendar[Calendar.MINUTE] }
        // Verify lines are drawn (ticks + hands)
        verify(atLeast = 1) { canvas.drawLine(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
        verify(atLeast = 1) { paint.strokeWidth = any() }
        // Exclude canvas rotation/save operations from verification
        excludeRecords { canvas.save() }
        excludeRecords { canvas.rotate(any(), any(), any()) }
        excludeRecords { canvas.restoreToCount(any()) }
    }

    @Test
    fun drawAnalogClock_withBorderGreaterThan2_drawsCircle() {
        val paint = mockk<Paint>()
        val canvas = mockk<Canvas>()
        val calendar = mockk<Calendar>()
        mockkStatic(::getColorPaint)
        mockkStatic(Calendar::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { Calendar.getInstance() } returns calendar
        every { calendar[Calendar.HOUR] } returns 10
        every { calendar[Calendar.MINUTE] } returns 30
        justRun { paint.style = any() }
        justRun { paint.strokeCap = any() }
        justRun { paint.strokeWidth = any() }
        justRun { canvas.drawCircle(any(), any(), any(), any()) }
        justRun { canvas.drawLine(any(), any(), any(), any(), any()) }
        every { canvas.save() } returns 0
        justRun { canvas.rotate(any(), any(), any()) }
        justRun { canvas.restoreToCount(any()) }

        val state = AnalogClockState(context)
        state.radius = 100F
        state.border = 5F
        state.tickCount = 12

        drawAnalogClock(state, canvas, 200, 200, 0)

        verify(exactly = 1) { canvas.drawCircle(any(), any(), 100F, paint) }
        verify(exactly = 1) { Calendar.getInstance() }
        verify(exactly = 1) { calendar[Calendar.HOUR] }
        verify(exactly = 1) { calendar[Calendar.MINUTE] }
        verify(atLeast = 1) { canvas.drawLine(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.strokeCap = Paint.Cap.ROUND }
        verify(atLeast = 1) { paint.strokeWidth = any() }
        // Exclude canvas rotation/save operations from verification
        excludeRecords { canvas.save() }
        excludeRecords { canvas.rotate(any(), any(), any()) }
        excludeRecords { canvas.restoreToCount(any()) }
    }
}
