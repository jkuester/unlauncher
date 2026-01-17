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
import com.jkuester.unlauncher.launchShowCalendar
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockBinaryBinding
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
    fun observeIs24HourFormatChanges_withTwentyFourHour_returnsTrue() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        )
        var initialValue: Boolean? = null
        var changedValue: Boolean? = null

        observeIs24HourFormatChanges(
            context,
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onFormatChanged = { changedValue = it }
        )

        initialValue shouldBe true
        changedValue shouldBe null
    }

    @Test
    fun observeIs24HourFormatChanges_withTwelveHour_returnsFalse() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        var initialValue: Boolean? = null
        var changedValue: Boolean? = null

        observeIs24HourFormatChanges(
            context,
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onFormatChanged = { changedValue = it }
        )

        initialValue shouldBe false
        changedValue shouldBe null
    }

    @Test
    fun observeIs24HourFormatChanges_withSystemFormat_usesDateFormat() {
        mockkStatic(DateFormat::class)
        every { DateFormat.is24HourFormat(context) } returns true

        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.system).build()
        )
        var initialValue: Boolean? = null

        observeIs24HourFormatChanges(
            context,
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onFormatChanged = { }
        )

        initialValue shouldBe true
        verify(exactly = 1) { DateFormat.is24HourFormat(context) }
    }

    @Test
    fun observeIs24HourFormatChanges_callsOnFormatChangedWhenFormatChanges() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        var initialValue: Boolean? = null
        val changedValues = mutableListOf<Boolean>()

        observeIs24HourFormatChanges(
            context,
            corePrefsRepo,
            onInitialValue = { initialValue = it },
            onFormatChanged = { changedValues.add(it) }
        )

        initialValue shouldBe false
        changedValues shouldBe emptyList()

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twenty_four_hour).build()
        }
        changedValues shouldBe listOf(true)

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        changedValues shouldBe listOf(true, false)
    }

    @Test
    fun observeIs24HourFormatChanges_doesNotCallOnFormatChangedWhenUnchanged() {
        val corePrefsRepo = TestDataRepository(
            CorePreferences.newBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        )
        var formatChangedCount = 0

        observeIs24HourFormatChanges(
            context,
            corePrefsRepo,
            onInitialValue = { },
            onFormatChanged = { formatChangedCount++ }
        )

        formatChangedCount shouldBe 0

        corePrefsRepo.updateAsync {
            it.toBuilder().setTimeFormat(TimeFormat.twelve_hour).build()
        }
        formatChangedCount shouldBe 0
    }
}
