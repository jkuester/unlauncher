package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datastore.proto.AnalogClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowCalendar
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockAnalogBinding
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlin.reflect.KFunction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class AnalogClockBindingsTest {
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

        updateAnalogClockDate(resources)(binding)

        verify(exactly = 1) { analogDate.text = "Fri, Jan 02" }
        verify(exactly = 1) { getCurrentDateString(resources) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.analog_date) }
    }

    @Test
    fun observeAnalogClockTypeChanges_withAnalog0_returns0() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_0).build()
        )
        var initialValue: Int? = null

        observeAnalogClockTypeChanges(
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onTickCountChanged = { }
        )

        initialValue shouldBe 0
    }

    @Test
    fun observeAnalogClockTypeChanges_withAnalog12_returns12() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        )
        var initialValue: Int? = null

        observeAnalogClockTypeChanges(
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onTickCountChanged = { }
        )

        initialValue shouldBe 12
    }

    @Test
    fun observeAnalogClockTypeChanges_withAnalog60_returns60() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_60).build()
        )
        var initialValue: Int? = null

        observeAnalogClockTypeChanges(
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onTickCountChanged = { }
        )

        initialValue shouldBe 60
    }

    @Test
    fun observeAnalogClockTypeChanges_callsOnTickCountChangedWhenTypeChanges() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        )
        var initialValue: Int? = null
        val changedValues = mutableListOf<Int>()

        observeAnalogClockTypeChanges(
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onTickCountChanged = { changedValues.add(it) }
        )

        initialValue shouldBe 12
        changedValues shouldBe emptyList()

        corePrefsRepo.updateAsync {
            it.toBuilder().setAnalogClockType(AnalogClockType.analog_60).build()
        }
        changedValues shouldBe listOf(60)

        corePrefsRepo.updateAsync {
            it.toBuilder().setAnalogClockType(AnalogClockType.analog_0).build()
        }
        changedValues shouldBe listOf(60, 0)
    }

    @Test
    fun observeAnalogClockTypeChanges_doesNotCallOnTickCountChangedWhenUnchanged() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        )
        var tickCountChangedCount = 0

        observeAnalogClockTypeChanges(
            corePrefsRepo,
            onInitialValue = { },
            onTickCountChanged = { tickCountChangedCount++ }
        )

        tickCountChangedCount shouldBe 0

        corePrefsRepo.updateAsync {
            it.toBuilder().setAnalogClockType(AnalogClockType.analog_12).build()
        }
        tickCountChangedCount shouldBe 0
    }

    @Test
    fun observeAnalogClockTypeChanges_allAnalogClockTypesMapCorrectly() {
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
            var initialValue: Int? = null

            observeAnalogClockTypeChanges(
                corePrefsRepo,
                onInitialValue = { initialValue = it },
                onTickCountChanged = { }
            )

            initialValue shouldBe expectedTickCount
        }
    }
}
