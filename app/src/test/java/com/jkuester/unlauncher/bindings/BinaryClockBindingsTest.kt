package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.format.DateFormat
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.getColorPaint
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowCalendar
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockBinaryBinding
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
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
class BinaryClockBindingsTest {
    @MockK
    lateinit var context: Context
    @MockK
    lateinit var resources: Resources
    @MockK
    lateinit var fragment: Fragment
    @MockK
    lateinit var rootView: LinearLayout
    @MockK
    lateinit var binaryDate: TextView

    private lateinit var binding: ClockBinaryBinding

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.binary_date) } returns binaryDate

        binding = ClockBinaryBinding.bind(rootView)
    }

    @Test
    fun setupBinaryClockDateClickListener_setsDateClickListener() {
        mockkStatic(::launchShowCalendar)
        val dateClickListener = mockk<OnClickListener>()
        every { launchShowCalendar(fragment) } returns dateClickListener
        justRun { binaryDate.setOnClickListener(any()) }

        setupBinaryClockDateClickListener(fragment)(binding)

        verify(exactly = 1) { launchShowCalendar(fragment) }
        verify(exactly = 1) { binaryDate.setOnClickListener(dateClickListener) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.binary_date) }
    }

    @Test
    fun updateBinaryClockDate_setsDateText() {
        mockkStatic(::getCurrentDateString)
        every { getCurrentDateString(resources) } returns "Fri, Jan 02"
        justRun { binaryDate.text = any() }

        updateBinaryClockDate(resources)(binding)

        verify(exactly = 1) { binaryDate.text = "Fri, Jan 02" }
        verify(exactly = 1) { getCurrentDateString(resources) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.binary_date) }
    }

    @Test
    fun observeIs24HourFormatChanges_withTwentyFourHour_setsStateToTrue() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        )
        val state = BinaryClockState(context)
        var formatChangedCount = 0

        observeIs24HourFormatChanges(context, corePrefsRepo, state) { formatChangedCount++ }

        state.is24Hour shouldBe true
        formatChangedCount shouldBe 0
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun observeIs24HourFormatChanges_withTwelveHour_setsStateToFalse() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        val state = BinaryClockState(context)
        var formatChangedCount = 0

        observeIs24HourFormatChanges(context, corePrefsRepo, state) { formatChangedCount++ }

        state.is24Hour shouldBe false
        formatChangedCount shouldBe 0
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun observeIs24HourFormatChanges_withSystemFormat_usesDateFormat() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        mockkStatic(DateFormat::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        every { DateFormat.is24HourFormat(context) } returns true

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.system).build()
        )
        val state = BinaryClockState(context)

        observeIs24HourFormatChanges(context, corePrefsRepo, state) { }

        state.is24Hour shouldBe true
        verify(exactly = 1) { DateFormat.is24HourFormat(context) }
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun observeIs24HourFormatChanges_callsOnFormatChangedWhenFormatChanges() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        val state = BinaryClockState(context)
        var formatChangedCount = 0

        observeIs24HourFormatChanges(context, corePrefsRepo, state) { formatChangedCount++ }

        state.is24Hour shouldBe false
        formatChangedCount shouldBe 0

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        }
        state.is24Hour shouldBe true
        formatChangedCount shouldBe 1

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        state.is24Hour shouldBe false
        formatChangedCount shouldBe 2
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun observeIs24HourFormatChanges_doesNotCallOnFormatChangedWhenUnchanged() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        val state = BinaryClockState(context)
        var formatChangedCount = 0

        observeIs24HourFormatChanges(context, corePrefsRepo, state) { formatChangedCount++ }

        formatChangedCount shouldBe 0

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        formatChangedCount shouldBe 0
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun binaryClockState_initializesWithDefaultValues() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }

        val state = BinaryClockState(context)

        state.centerPoint shouldBe Pair(0F, 0F)
        state.bitSize shouldBe 20F
        state.distance shouldBe 10F
        state.is24Hour shouldBe false
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun updateStateOnSizeChanged_updatesCenterPointDistanceAndBitSize() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        mockkConstructor(RectF::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { anyConstructed<RectF>().set(any(), any(), any(), any()) } answers { }
        justRun { paint.style = any() }
        excludeRecords { anyConstructed<RectF>().set(any(), any(), any(), any()) }

        val state = BinaryClockState(context)

        updateStateOnSizeChanged(state, 500, 200, 500)

        state.centerPoint shouldBe Pair(250F, 100F)
        state.distance shouldBe 10F // 500 / 50
        state.bitSize shouldBe 20F // distance * 2
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
    }

    @Test
    fun updateStateOnSizeChanged_updatesHourAndMinuteBounds() {
        val paint = mockk<Paint>()
        mockkStatic(::getColorPaint)
        mockkConstructor(RectF::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { anyConstructed<RectF>().set(any(), any(), any(), any()) } answers { }
        justRun { paint.style = any() }

        val state = BinaryClockState(context)

        updateStateOnSizeChanged(state, 1000, 400, 1000)

        // distance = 20, bitSize = 40
        // bitWidth = 20 + 2*40 = 100
        // bitHeight = 20*3 + 40*2 = 140
        // startX = -500 + 100*3 = -200
        // startY = 200 - 140*2 = -80
        state.centerPoint shouldBe Pair(500F, 200F)
        state.distance shouldBe 20F
        state.bitSize shouldBe 40F
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
        // hourBounds: set(startX, startY, startX + viewWidth, startY + bitHeight)
        //           = set(-200, -80, -200 + 1000, -80 + 140) = set(-200, -80, 800, 60)
        verify(exactly = 1) { anyConstructed<RectF>().set(-200F, -80F, 800F, 60F) }
        // minuteBounds: set(startX, startY + bitHeight, startX + viewWidth, startY + bitHeight * 2)
        //             = set(-200, -80 + 140, -200 + 1000, -80 + 280) = set(-200, 60, 800, 200)
        verify(exactly = 1) { anyConstructed<RectF>().set(-200F, 60F, 800F, 200F) }
        excludeRecords { anyConstructed<RectF>().set(any(), any(), any(), any()) }
    }

    @Test
    fun calculateMinimumDimensions_returnsCorrectDimensions() {
        val paint = mockk<Paint>()
        val view = mockk<View>()
        mockkStatic(::getColorPaint)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        justRun { paint.style = any() }
        every { view.paddingLeft } returns 10
        every { view.paddingRight } returns 10
        every { view.paddingTop } returns 5
        every { view.paddingBottom } returns 5

        val state = BinaryClockState(context)
        // Default state: bitSize = 20, distance = 10

        val (minWidth, minHeight) = calculateMinimumDimensions(state, view, 100)

        // minWidth = 10 + 10 + 100 + 12*20 + 7*10 = 120 + 240 + 70 = 430
        minWidth shouldBe 430
        // minHeight = 5 + 5 + 4*20 + 5*10 = 10 + 80 + 50 = 140
        minHeight shouldBe 140
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
        verify(exactly = 1) { view.paddingLeft }
        verify(exactly = 1) { view.paddingRight }
        verify(exactly = 1) { view.paddingTop }
        verify(exactly = 1) { view.paddingBottom }
    }

    @Test
    fun drawBinaryClock_in12HourMode_rendersWithCorrectBits() {
        val paint = mockk<Paint>()
        val canvas = mockk<Canvas>()
        val calendar = mockk<Calendar>()
        mockkStatic(::getColorPaint)
        mockkStatic(Calendar::class)
        mockkConstructor(RectF::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { Calendar.getInstance() } returns calendar
        every { calendar[Calendar.HOUR] } returns 10
        every { calendar[Calendar.MINUTE] } returns 30
        every { calendar[Calendar.AM_PM] } returns Calendar.AM
        justRun { paint.style = any() }
        justRun { canvas.drawCircle(any(), any(), any(), any()) }
        every { anyConstructed<RectF>().height() } returns 100F

        val state = BinaryClockState(context)
        state.is24Hour = false
        state.hourBounds.right = 100F
        state.hourBounds.bottom = 100F
        state.minuteBounds.right = 100F
        state.minuteBounds.bottom = 100F

        drawBinaryClock(state, canvas)

        // 4 bits for hour (12-hour mode) + 6 bits for minute = 10 circles
        verify(exactly = 10) { canvas.drawCircle(any(), any(), any(), any()) }
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
        verify(exactly = 1) { Calendar.getInstance() }
        verify(exactly = 1) { calendar[Calendar.HOUR] }
        verify(exactly = 1) { calendar[Calendar.MINUTE] }
        verify(exactly = 1) { calendar[Calendar.AM_PM] }
        excludeRecords { anyConstructed<RectF>().height() }
    }

    @Test
    fun drawBinaryClock_in24HourMode_rendersWithCorrectBits() {
        val paint = mockk<Paint>()
        val canvas = mockk<Canvas>()
        val calendar = mockk<Calendar>()
        mockkStatic(::getColorPaint)
        mockkStatic(Calendar::class)
        mockkConstructor(RectF::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { Calendar.getInstance() } returns calendar
        every { calendar[Calendar.HOUR_OF_DAY] } returns 14
        every { calendar[Calendar.MINUTE] } returns 45
        every { calendar[Calendar.AM_PM] } returns Calendar.PM
        justRun { paint.style = any() }
        justRun { canvas.drawCircle(any(), any(), any(), any()) }
        every { anyConstructed<RectF>().height() } returns 100F

        val state = BinaryClockState(context)
        state.is24Hour = true
        state.hourBounds.right = 100F
        state.hourBounds.bottom = 100F
        state.minuteBounds.right = 100F
        state.minuteBounds.bottom = 100F

        drawBinaryClock(state, canvas)

        // 5 bits for hour (24-hour mode) + 6 bits for minute = 11 circles
        verify(exactly = 11) { canvas.drawCircle(any(), any(), any(), any()) }
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
        verify(exactly = 1) { Calendar.getInstance() }
        verify(exactly = 1) { calendar[Calendar.HOUR_OF_DAY] }
        verify(exactly = 1) { calendar[Calendar.MINUTE] }
        verify(exactly = 1) { calendar[Calendar.AM_PM] }
        excludeRecords { anyConstructed<RectF>().height() }
    }

    @Test
    fun drawBinaryClock_withZeroHourPM_displaysAs12() {
        val paint = mockk<Paint>()
        val canvas = mockk<Canvas>()
        val calendar = mockk<Calendar>()
        mockkStatic(::getColorPaint)
        mockkStatic(Calendar::class)
        mockkConstructor(RectF::class)
        every { getColorPaint(context, R.attr.colorAccent) } returns paint
        every { Calendar.getInstance() } returns calendar
        // hour=0, AM_PM=PM means 12 PM (noon) in 12-hour mode
        every { calendar[Calendar.HOUR] } returns 0
        every { calendar[Calendar.MINUTE] } returns 0
        every { calendar[Calendar.AM_PM] } returns Calendar.PM
        justRun { paint.style = any() }
        justRun { canvas.drawCircle(any(), any(), any(), any()) }
        every { anyConstructed<RectF>().height() } returns 100F

        val state = BinaryClockState(context)
        state.is24Hour = false
        state.hourBounds.right = 100F
        state.hourBounds.bottom = 100F
        state.minuteBounds.right = 100F
        state.minuteBounds.bottom = 100F

        drawBinaryClock(state, canvas)

        // Should render 12 (binary 1100) for hours, 0 for minutes
        // 4 bits for hour + 6 bits for minute = 10 circles total
        verify(exactly = 10) { canvas.drawCircle(any(), any(), any(), any()) }
        verify(exactly = 2) { getColorPaint(context, R.attr.colorAccent) }
        verify(exactly = 1) { paint.style = Paint.Style.STROKE }
        verify(exactly = 1) { paint.style = Paint.Style.FILL_AND_STROKE }
        verify(exactly = 1) { Calendar.getInstance() }
        verify(exactly = 1) { calendar[Calendar.HOUR] }
        verify(exactly = 1) { calendar[Calendar.MINUTE] }
        verify(exactly = 1) { calendar[Calendar.AM_PM] }
        excludeRecords { anyConstructed<RectF>().height() }
    }
}
