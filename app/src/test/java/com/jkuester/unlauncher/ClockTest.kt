package com.jkuester.unlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.res.Resources
import android.view.View
import com.jkuester.unlauncher.android.view.AnalogClockView
import com.jkuester.unlauncher.android.view.BinaryClockView
import com.jkuester.unlauncher.android.view.DigitalClockView
import com.jkuester.unlauncher.android.view.createAnalogClockView
import com.jkuester.unlauncher.android.view.createBinaryClockView
import com.jkuester.unlauncher.android.view.createDigitalClockView
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.sduduzog.slimlauncher.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class ClockTest {
    @MockK
    lateinit var context: Context

    @Nested
    inner class GetCurrentDateStringTest {
        @MockK
        lateinit var resources: Resources

        @Test
        fun getCurrentDateString_formatsWithResourceDateFormat() {
            every { resources.getString(R.string.main_date_format) } returns "EEE, MMM dd"

            val result = getCurrentDateString(resources)

            // Result should be a formatted date string matching the pattern (e.g., "Fri, Jan 17")
            result shouldMatch Regex("[A-Za-z]{3}, [A-Za-z]{3} \\d{2}")
            verify(exactly = 1) { resources.getString(R.string.main_date_format) }
        }

        @Test
        fun getCurrentDateString_withDifferentFormat_formatsCorrectly() {
            every { resources.getString(R.string.main_date_format) } returns "yyyy-MM-dd"

            val result = getCurrentDateString(resources)

            // Result should be a formatted date string matching the pattern (e.g., "2026-01-17")
            result shouldMatch Regex("\\d{4}-\\d{2}-\\d{2}")
            verify(exactly = 1) { resources.getString(R.string.main_date_format) }
        }
    }

    @Nested
    inner class CreateNewClockTest {
        @Test
        fun returnsAnalogClock() {
            val analogClockView = mockk<AnalogClockView>()
            mockkStatic(::createAnalogClockView)
            every { createAnalogClockView(any()) } returns analogClockView

            val clock = createNewClock(context, ClockType.analog)

            clock shouldBe analogClockView
            verify(exactly = 1) { createAnalogClockView(context) }
        }

        @Test
        fun returnsDigitalClock() {
            val digitalClockView = mockk<DigitalClockView>()
            mockkStatic(::createDigitalClockView)
            every { createDigitalClockView(any()) } returns digitalClockView

            val clock = createNewClock(context, ClockType.digital)

            clock shouldBe digitalClockView
            verify(exactly = 1) { createDigitalClockView(context) }
        }

        @Test
        fun returnsBinaryClock() {
            val binaryClockView = mockk<BinaryClockView>()
            mockkStatic(::createBinaryClockView)
            every { createBinaryClockView(any()) } returns binaryClockView

            val clock = createNewClock(context, ClockType.binary)

            clock shouldBe binaryClockView
            verify(exactly = 1) { createBinaryClockView(context) }
        }
    }

    @Nested
    inner class ClockReceiverTest {
        @MockK
        lateinit var clockView: View

        @BeforeEach
        fun beforeEach() {
            mockkConstructor(IntentFilter::class)
            justRun { anyConstructed<IntentFilter>().addAction(any()) }
        }

        @Test
        fun onReceive_withClock_requestsLayoutAndInvalidates() {
            justRun { clockView.requestLayout() }
            justRun { clockView.invalidate() }
            val receiver = ClockReceiver()
            receiver.clock = clockView

            receiver.onReceive(context, null)

            verify(exactly = 1) { clockView.requestLayout() }
            verify(exactly = 1) { clockView.invalidate() }
        }

        @Test
        fun onReceive_withNullClock_doesNothing() {
            val receiver = ClockReceiver()
            receiver.clock = null

            receiver.onReceive(context, null)

            // No exceptions and no interactions with any views
        }

        @Test
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun register_registersReceiverAndTriggersOnReceive() {
            every { context.registerReceiver(any(), any<IntentFilter>()) } returns mockk()
            val receiver = ClockReceiver()

            receiver.register(context)

            verify(exactly = 1) { context.registerReceiver(receiver, any<IntentFilter>()) }
        }

        @Test
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun register_withClock_triggersLayoutUpdate() {
            every { context.registerReceiver(any(), any<IntentFilter>()) } returns mockk()
            justRun { clockView.requestLayout() }
            justRun { clockView.invalidate() }
            val receiver = ClockReceiver()
            receiver.clock = clockView

            receiver.register(context)

            verify(exactly = 1) { context.registerReceiver(receiver, any<IntentFilter>()) }
            verify(exactly = 1) { clockView.requestLayout() }
            verify(exactly = 1) { clockView.invalidate() }
        }

        @Test
        fun unregister_unregistersReceiver() {
            justRun { context.unregisterReceiver(any()) }
            val receiver = ClockReceiver()

            receiver.unregister(context)

            verify(exactly = 1) { context.unregisterReceiver(receiver) }
        }

        @Test
        fun clock_canBeSetAndRetrieved() {
            val receiver = ClockReceiver()

            receiver.clock shouldBe null

            receiver.clock = clockView

            receiver.clock shouldBe clockView
        }
    }
}
