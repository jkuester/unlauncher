package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowAlarms
import com.jkuester.unlauncher.launchShowCalendar
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockDigitalBinding
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlin.reflect.KFunction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class DigitalClockBindingsTest {
    @MockK
    lateinit var context: Context
    @MockK
    lateinit var resources: Resources
    @MockK
    lateinit var fragment: Fragment
    @MockK
    lateinit var rootView: LinearLayout
    @MockK
    lateinit var digitalTime: TextView
    @MockK
    lateinit var digitalDate: TextView

    private lateinit var binding: ClockDigitalBinding

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.digital_time) } returns digitalTime
        every { ViewBindings.findChildViewById<View>(any(), R.id.digital_date) } returns digitalDate

        binding = ClockDigitalBinding.bind(rootView)
    }

    @Test
    fun setupDigitalClockClickListeners_setsTimeClickListener() {
        mockkStatic(::launchShowAlarms)
        mockkStatic(::launchShowCalendar)
        val timeClickListener = mockk<OnClickListener>()
        val dateClickListener = mockk<OnClickListener>()
        every { launchShowAlarms(fragment) } returns timeClickListener
        every { launchShowCalendar(fragment) } returns dateClickListener
        justRun { digitalTime.setOnClickListener(any()) }
        justRun { digitalDate.setOnClickListener(any()) }

        setupDigitalClockClickListeners(fragment)(binding)

        verify(exactly = 1) { launchShowAlarms(fragment) }
        verify(exactly = 1) { launchShowCalendar(fragment) }
        verify(exactly = 1) { digitalTime.setOnClickListener(timeClickListener) }
        verify(exactly = 1) { digitalDate.setOnClickListener(dateClickListener) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_time) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_date) }
    }

    @Test
    fun updateDigitalClockViews_withTwentyFourHourFormat_usesCorrectPattern() {
        mockkStatic(::getCurrentDateString)
        every { getCurrentDateString(resources) } returns "Fri, Jan 02"
        val timeSlot = slot<CharSequence>()
        justRun { digitalTime.text = capture(timeSlot) }
        justRun { digitalDate.text = any() }
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        )

        val updateFn = updateDigitalClockViews(context, resources, corePrefsRepo, binding)
        updateFn()

        // 24-hour format pattern "H:mm" produces times like "14:30" or "9:05"
        timeSlot.captured.toString() shouldMatch Regex("\\d{1,2}:\\d{2}")
        verify(exactly = 1) { digitalTime.text = timeSlot.captured }
        verify(exactly = 1) { digitalDate.text = "Fri, Jan 02" }
        verify(exactly = 1) { getCurrentDateString(resources) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_time) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_date) }
    }

    @Test
    fun updateDigitalClockViews_withTwelveHourFormat_usesCorrectPattern() {
        mockkStatic(::getCurrentDateString)
        every { getCurrentDateString(resources) } returns "Fri, Jan 02"
        val timeSlot = slot<CharSequence>()
        justRun { digitalTime.text = capture(timeSlot) }
        justRun { digitalDate.text = any() }
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )

        val updateFn = updateDigitalClockViews(context, resources, corePrefsRepo, binding)
        updateFn()

        // 12-hour format pattern "h:mm aa" produces times like "2:30 PM" or "9:05 AM"
        timeSlot.captured.toString() shouldMatch Regex("\\d{1,2}:\\d{2} [AP]M")
        verify(exactly = 1) { digitalTime.text = timeSlot.captured }
        verify(exactly = 1) { digitalDate.text = "Fri, Jan 02" }
        verify(exactly = 1) { getCurrentDateString(resources) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_time) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_date) }
    }

    @Test
    fun updateDigitalClockViews_withSystemFormat_usesDateFormat() {
        mockkStatic(::getCurrentDateString)
        mockkStatic(DateFormat::class)
        every { getCurrentDateString(resources) } returns "Fri, Jan 02"
        val systemTimeFormat = mockk<java.text.DateFormat>()
        every { DateFormat.getTimeFormat(context) } returns systemTimeFormat
        every { systemTimeFormat.format(any()) } returns "12:00 PM"
        justRun { digitalTime.text = any() }
        justRun { digitalDate.text = any() }
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.system).build()
        )

        val updateFn = updateDigitalClockViews(context, resources, corePrefsRepo, binding)
        updateFn()

        verify(exactly = 1) { digitalTime.text = "12:00 PM" }
        verify(exactly = 1) { digitalDate.text = "Fri, Jan 02" }
        verify(exactly = 1) { getCurrentDateString(resources) }
        verify(exactly = 1) { DateFormat.getTimeFormat(context) }
        verify(exactly = 1) { systemTimeFormat.format(any()) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_time) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_date) }
    }

    @Test
    fun updateDigitalClockViews_returnsReusableFunction() {
        mockkStatic(::getCurrentDateString)
        mockkStatic(DateFormat::class)
        every { getCurrentDateString(resources) } returns "Fri, Jan 02"
        val systemTimeFormat = mockk<java.text.DateFormat>()
        every { DateFormat.getTimeFormat(context) } returns systemTimeFormat
        every { systemTimeFormat.format(any()) } returns "12:00 PM"
        justRun { digitalTime.text = any() }
        justRun { digitalDate.text = any() }
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.system).build()
        )

        val updateFn = updateDigitalClockViews(context, resources, corePrefsRepo, binding)
        updateFn()
        updateFn()
        updateFn()

        verify(exactly = 3) { digitalTime.text = "12:00 PM" }
        verify(exactly = 3) { digitalDate.text = "Fri, Jan 02" }
        verify(exactly = 3) { getCurrentDateString(resources) }
        verify(exactly = 3) { DateFormat.getTimeFormat(context) }
        verify(exactly = 3) { systemTimeFormat.format(any()) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_time) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.digital_date) }
    }

    @Test
    fun observeTimeFormatChanges_callsOnUpdateWhenFormatChanges() {
        val corePrefsRepo = TestDataRepository(CorePreferences.getDefaultInstance())
        var updateCount = 0

        observeTimeFormatChanges(corePrefsRepo) { updateCount++ }

        updateCount shouldBe 0 // Initial observation should not trigger update

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        }
        updateCount shouldBe 1

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        updateCount shouldBe 2
    }

    @Test
    fun observeTimeFormatChanges_doesNotCallOnUpdateWhenFormatUnchanged() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        var updateCount = 0

        observeTimeFormatChanges(corePrefsRepo) { updateCount++ }

        updateCount shouldBe 0 // Initial observation should not trigger update

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        updateCount shouldBe 0 // Same format, no update
    }
}
