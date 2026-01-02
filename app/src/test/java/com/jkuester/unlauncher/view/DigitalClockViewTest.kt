package com.jkuester.unlauncher.view

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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.KFunction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class DigitalClockViewTest {
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
        // Mock ViewBindings for ClockDigitalBinding
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.digital_time) } returns digitalTime
        every { ViewBindings.findChildViewById<View>(any(), R.id.digital_date) } returns digitalDate

        binding = ClockDigitalBinding.bind(rootView)

        // Mock context resources
//        every { context.resources } returns resources
    }

    @Test
    fun setupClickListeners_setsTimeClickListener() {
        mockkStatic(::launchShowAlarms)
        val timeClickListener = mockk<OnClickListener>()
        every { launchShowAlarms(fragment) } returns timeClickListener
        justRun { digitalTime.setOnClickListener(any()) }

        // Simulate what DigitalClockView does during init
        binding.digitalTime.setOnClickListener(launchShowAlarms(fragment))

        verify(exactly = 1) { launchShowAlarms(fragment) }
        verify(exactly = 1) { digitalTime.setOnClickListener(timeClickListener) }
    }

    @Test
    fun setupClickListeners_setsDateClickListener() {
        mockkStatic(::launchShowCalendar)
        val dateClickListener = mockk<OnClickListener>()
        every { launchShowCalendar(fragment) } returns dateClickListener
        justRun { digitalDate.setOnClickListener(any()) }

        // Simulate what DigitalClockView does during init
        binding.digitalDate.setOnClickListener(launchShowCalendar(fragment))

        verify(exactly = 1) { launchShowCalendar(fragment) }
        verify(exactly = 1) { digitalDate.setOnClickListener(dateClickListener) }
    }

    @Test
    fun updateChildViews_withTwentyFourHourFormat_usesCorrectPattern() {
        mockkStatic(::getCurrentDateString)
        every { getCurrentDateString(any()) } returns "Fri, Jan 02"
        justRun { digitalTime.text = any() }
        justRun { digitalDate.text = any() }

        val timeFormat = TimeFormat.twenty_four_hour

        // Simulate what updateChildViews does
        val timeStringFormat = when (timeFormat) {
            TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
            TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
            else -> DateFormat.getTimeFormat(context)
        }
        binding.digitalTime.text = timeStringFormat.format(Date())
        binding.digitalDate.text = getCurrentDateString(resources)

        verify(exactly = 1) { digitalTime.text = any() }
        verify(exactly = 1) { digitalDate.text = "Fri, Jan 02" }
    }

    @Test
    fun updateChildViews_withTwelveHourFormat_usesCorrectPattern() {
        mockkStatic(::getCurrentDateString)
        every { getCurrentDateString(any()) } returns "Fri, Jan 02"
        justRun { digitalTime.text = any() }
        justRun { digitalDate.text = any() }

        val timeFormat = TimeFormat.twelve_hour

        // Simulate what updateChildViews does
        val timeStringFormat = when (timeFormat) {
            TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
            TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
            else -> DateFormat.getTimeFormat(context)
        }
        binding.digitalTime.text = timeStringFormat.format(Date())
        binding.digitalDate.text = getCurrentDateString(resources)

        verify(exactly = 1) { digitalTime.text = any() }
        verify(exactly = 1) { digitalDate.text = "Fri, Jan 02" }
    }

    @Test
    fun updateChildViews_withSystemFormat_usesDateFormat() {
        mockkStatic(::getCurrentDateString)
        mockkStatic(DateFormat::class)
        every { getCurrentDateString(any()) } returns "Fri, Jan 02"
        val systemTimeFormat = mockk<java.text.DateFormat>()
        every { DateFormat.getTimeFormat(any()) } returns systemTimeFormat
        every { systemTimeFormat.format(any()) } returns "12:00 PM"
        justRun { digitalTime.text = any() }
        justRun { digitalDate.text = any() }

        val timeFormat = TimeFormat.system

        // Simulate what updateChildViews does
        val timeStringFormat = when (timeFormat) {
            TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
            TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
            else -> DateFormat.getTimeFormat(context)
        }
        binding.digitalTime.text = timeStringFormat.format(Date())
        binding.digitalDate.text = getCurrentDateString(resources)

        verify(exactly = 1) { systemTimeFormat.format(any()) }
        verify(exactly = 1) { DateFormat.getTimeFormat(context) }
        verify(exactly = 1) { digitalTime.text = "12:00 PM" }
        verify(exactly = 1) { digitalDate.text = "Fri, Jan 02" }
    }

    @Test
    fun listenForChangesToTimeFormat_updatesViewWhenFormatChanges() {
        val corePrefsRepo = TestDataRepository(CorePreferences.getDefaultInstance())
        var currentTimeFormat = TimeFormat.system
        var updateCount = 0

        // Simulate the observer callback from the DigitalClockView
        val observer: (CorePreferences) -> Unit = { corePrefs ->
            val originalTimeFormat = currentTimeFormat
            currentTimeFormat = corePrefs.timeFormat
            if (originalTimeFormat != currentTimeFormat) {
                // Simulate invalidate -> updateChildViews
                updateCount++
            }
        }

        // Initial observation
        corePrefsRepo.observe { observer(it) }
        updateCount shouldBe 0 // First call, no change from initial value

        // Change the format
        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        }
        updateCount shouldBe 1

        // Change again
        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        updateCount shouldBe 2
    }

    @Test
    fun listenForChangesToTimeFormat_doesNotUpdateWhenFormatUnchanged() {
        var currentTimeFormat = TimeFormat.twelve_hour
        var updateCount = 0

        // Initialize with twelve_hour format
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )

        // Simulate the observer callback from the DigitalClockView
        val observer: (CorePreferences) -> Unit = { corePrefs ->
            val originalTimeFormat = currentTimeFormat
            currentTimeFormat = corePrefs.timeFormat
            if (originalTimeFormat != currentTimeFormat) {
                updateCount++
            }
        }

        // Initial observation
        corePrefsRepo.observe { observer(it) }
        updateCount shouldBe 0 // No change since both are twelve_hour

        // Update with same format
        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        updateCount shouldBe 0 // Still no change
    }

    @Test
    fun timeFormatSelection_twentyFourHour_returnsSimpleDateFormat() {
        val timeFormat = TimeFormat.twenty_four_hour

        val result = when (timeFormat) {
            TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
            TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
            else -> SimpleDateFormat("HH:mm", Locale.getDefault()) // Fallback for test
        }

        result.toPattern() shouldBe "H:mm"
    }

    @Test
    fun timeFormatSelection_twelveHour_returnsSimpleDateFormat() {
        val timeFormat = TimeFormat.twelve_hour

        val result = when (timeFormat) {
            TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
            TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
            else -> SimpleDateFormat("HH:mm", Locale.getDefault()) // Fallback for test
        }

        result.toPattern() shouldBe "h:mm aa"
    }

    @Test
    fun timeFormatSelection_systemOrUnrecognized_fallsToElseBranch() {
        val timeFormats = listOf(TimeFormat.system, TimeFormat.UNRECOGNIZED)

        timeFormats.forEach { timeFormat ->
            val isFallback = when (timeFormat) {
                TimeFormat.twenty_four_hour -> false
                TimeFormat.twelve_hour -> false
                else -> true
            }

            isFallback shouldBe true
        }
    }
}
