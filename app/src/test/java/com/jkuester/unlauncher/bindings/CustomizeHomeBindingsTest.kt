package com.jkuester.unlauncher.bindings

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.adapter.CustomizeHomeAppsListAdapter
import com.jkuester.unlauncher.createNewClock
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.addHomeApp
import com.jkuester.unlauncher.datasource.setAnalogClockType
import com.jkuester.unlauncher.datasource.setCenterIconId
import com.jkuester.unlauncher.datasource.setClockType
import com.jkuester.unlauncher.datasource.setHomeApps
import com.jkuester.unlauncher.datasource.setLeftIconId
import com.jkuester.unlauncher.datasource.setRightIconId
import com.jkuester.unlauncher.datastore.proto.AnalogClockType
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.dialog.AnalogClockTypeDialog
import com.jkuester.unlauncher.dialog.ClockTypeDialog
import com.jkuester.unlauncher.dialog.QuickButtonIconDialog
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeHomeBinding
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlin.reflect.KFunction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification Weird bug between mockk/kotlin/Java is causing this to fail
@ExtendWith(MockKExtension::class)
class CustomizeHomeBindingsTest {
    @MockK
    lateinit var rootView: ConstraintLayout
    @MockK
    lateinit var headerBack: ImageView
    @MockK
    lateinit var headerTitle: TextView
    @MockK
    lateinit var homeAppsList: RecyclerView
    @MockK
    lateinit var addHomeAppButton: LinearLayout
    @MockK
    lateinit var quickButtonLeft: ImageView
    @MockK
    lateinit var quickButtonCenter: ImageView
    @MockK
    lateinit var quickButtonRight: ImageView
    @MockK
    lateinit var clockWrapper: FrameLayout
    @MockK
    lateinit var clockOptionsButton: ImageView

    private lateinit var binding: CustomizeHomeBinding
    private val prefsRepo = TestDataRepository(QuickButtonPreferences.getDefaultInstance())
    private val appsRepo = TestDataRepository(UnlauncherApps.getDefaultInstance())
    private val corePrefsRepo = TestDataRepository(CorePreferences.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_back) } returns headerBack
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_title) } returns headerTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.customise_home_apps_list) } returns homeAppsList
        every { ViewBindings.findChildViewById<View>(any(), R.id.add_home_app) } returns addHomeAppButton
        every { ViewBindings.findChildViewById<View>(any(), R.id.quick_button_left) } returns quickButtonLeft
        every { ViewBindings.findChildViewById<View>(any(), R.id.quick_button_center) } returns quickButtonCenter
        every { ViewBindings.findChildViewById<View>(any(), R.id.quick_button_right) } returns quickButtonRight
        every { ViewBindings.findChildViewById<View>(any(), R.id.clock_wrapper) } returns clockWrapper
        every { ViewBindings.findChildViewById<View>(any(), R.id.clock_options_button) } returns clockOptionsButton

        binding = CustomizeHomeBinding.bind(rootView)
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_back) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.customise_home_apps_list) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.add_home_app) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.quick_button_left) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.quick_button_center) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.quick_button_right) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.clock_wrapper) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.clock_options_button) }
    }

    @Test
    fun setupCustomizeQuickButtonsBackButton() {
        val activity = mockk<FragmentActivity>()
        val onBackPressedDispatcher = mockk<OnBackPressedDispatcher>()
        every { activity.onBackPressedDispatcher } returns onBackPressedDispatcher
        justRun { onBackPressedDispatcher.onBackPressed() }
        val clickListenerSlot = slot<OnClickListener>()
        justRun { headerBack.setOnClickListener(capture(clickListenerSlot)) }

        setupCustomizeQuickButtonsBackButton(activity)(binding)

        verify(exactly = 1) { headerBack.setOnClickListener(clickListenerSlot.captured) }

        clickListenerSlot.captured.onClick(headerBack)

        verify(exactly = 1) { activity.onBackPressedDispatcher }
        verify(exactly = 1) { onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun setupQuickButtonIcons() {
        val leftButtonClickListenerSlot = slot<OnClickListener>()
        val centerButtonClickListenerSlot = slot<OnClickListener>()
        val rightButtonClickListenerSlot = slot<OnClickListener>()
        justRun { quickButtonLeft.setOnClickListener(capture(leftButtonClickListenerSlot)) }
        justRun { quickButtonCenter.setOnClickListener(capture(centerButtonClickListenerSlot)) }
        justRun { quickButtonRight.setOnClickListener(capture(rightButtonClickListenerSlot)) }
        justRun { quickButtonLeft.setImageResource(any()) }
        justRun { quickButtonCenter.setImageResource(any()) }
        justRun { quickButtonRight.setImageResource(any()) }
        justRun { quickButtonLeft.setBackgroundResource(any()) }
        justRun { quickButtonCenter.setBackgroundResource(any()) }
        justRun { quickButtonRight.setBackgroundResource(any()) }
        val fragmentManager = mockk<FragmentManager>()
        mockkConstructor(QuickButtonIconDialog::class)
        justRun { anyConstructed<QuickButtonIconDialog>().showNow(any(), any()) }

        setupQuickButtonIcons(prefsRepo, fragmentManager)(binding)

        verify(exactly = 1) { quickButtonLeft.setOnClickListener(leftButtonClickListenerSlot.captured) }
        verify(exactly = 1) { quickButtonCenter.setOnClickListener(centerButtonClickListenerSlot.captured) }
        verify(exactly = 1) { quickButtonRight.setOnClickListener(rightButtonClickListenerSlot.captured) }

        // Simulate tapping icons
        leftButtonClickListenerSlot.captured.onClick(quickButtonLeft)
        verify(exactly = 1) { QuickButtonIconDialog(QuickButtonIcon.IC_CALL.prefId).showNow(fragmentManager, null) }
        centerButtonClickListenerSlot.captured.onClick(quickButtonCenter)
        verify(exactly = 2) { QuickButtonIconDialog(QuickButtonIcon.IC_COG.prefId).showNow(fragmentManager, null) }
        rightButtonClickListenerSlot.captured.onClick(quickButtonRight)
        verify(exactly = 3) {
            QuickButtonIconDialog(QuickButtonIcon.IC_PHOTO_CAMERA.prefId).showNow(fragmentManager, null)
        }

        // Simulate setting show icon
        prefsRepo.updateAsync(setLeftIconId(2))
        verify(exactly = 1) { quickButtonLeft.setImageResource(QuickButtonIcon.IC_CALL.resourceId) }
        verify(exactly = 1) { quickButtonLeft.setBackgroundResource(0) }
        prefsRepo.updateAsync(setCenterIconId(3))
        verify(exactly = 1) { quickButtonCenter.setImageResource(QuickButtonIcon.IC_COG.resourceId) }
        verify(exactly = 1) { quickButtonCenter.setBackgroundResource(0) }
        prefsRepo.updateAsync(setRightIconId(4))
        verify(exactly = 1) { quickButtonRight.setImageResource(QuickButtonIcon.IC_PHOTO_CAMERA.resourceId) }
        verify(exactly = 1) { quickButtonRight.setBackgroundResource(0) }

        // Simulate setting hide icon
        prefsRepo.updateAsync(setLeftIconId(1))
        verify(exactly = 1) { quickButtonLeft.setImageResource(QuickButtonIcon.IC_EMPTY.resourceId) }
        verify(exactly = 1) { quickButtonLeft.setBackgroundResource(R.drawable.imageview_border) }
        prefsRepo.updateAsync(setCenterIconId(1))
        verify(exactly = 1) { quickButtonCenter.setImageResource(QuickButtonIcon.IC_EMPTY.resourceId) }
        verify(exactly = 1) { quickButtonCenter.setBackgroundResource(R.drawable.imageview_border) }
        prefsRepo.updateAsync(setRightIconId(1))
        verify(exactly = 1) { quickButtonRight.setImageResource(QuickButtonIcon.IC_EMPTY.resourceId) }
        verify(exactly = 1) { quickButtonRight.setBackgroundResource(R.drawable.imageview_border) }
    }

    @Test
    fun setupAddHomeAppButton() {
        mockkStatic(Navigation::class)
        val navigatorClickListener = mockk<OnClickListener>()
        every { Navigation.createNavigateOnClickListener(any<Int>()) } returns navigatorClickListener
        justRun { addHomeAppButton.setOnClickListener(any()) }
        justRun { addHomeAppButton.visibility = any() }
        val originalApps = (0..5).map { UnlauncherApp.newBuilder().setPackageName(it.toString()).build() }
        appsRepo.updateAsync { it.toBuilder().addAllApps(originalApps).build() }

        setupAddHomeAppButton(appsRepo)(binding)

        verify(exactly = 1) {
            Navigation.createNavigateOnClickListener(
                R.id.customiseQuickButtonsFragment_to_customizeHomeAppsAddAppFragment
            )
        }
        verify(exactly = 1) { addHomeAppButton.setOnClickListener(navigatorClickListener) }
        verify(exactly = 1) { addHomeAppButton.visibility = View.VISIBLE }

        // Simulate adding 5 home apps
        appsRepo.updateAsync(setHomeApps(originalApps.subList(0, 5)))
        verify(exactly = 2) { addHomeAppButton.visibility = View.VISIBLE }

        // Simulate adding a 6th home app
        appsRepo.updateAsync(addHomeApp(originalApps[5]))

        verify(exactly = 1) { addHomeAppButton.visibility = View.GONE }
    }

    @Test
    fun setupHomeAppsList() {
        justRun { homeAppsList.adapter = any() }

        setupHomeAppsList(appsRepo, mockk())(binding)

        verify(exactly = 1) { homeAppsList.adapter = any<CustomizeHomeAppsListAdapter>() }
    }

    @Test
    fun setupClockPreview() {
        val context = mockk<Context>()
        val fragmentManager = mockk<FragmentManager>()
        val clickListenerSlot = slot<OnClickListener>()
        val optionsClickListenerSlot = slot<OnClickListener>()
        justRun { clockWrapper.setOnClickListener(capture(clickListenerSlot)) }
        justRun { clockOptionsButton.setOnClickListener(capture(optionsClickListenerSlot)) }
        justRun { clockWrapper.removeAllViews() }
        justRun { clockWrapper.addView(any()) }
        justRun { clockWrapper.setBackgroundResource(any()) }
        justRun { clockOptionsButton.visibility = any() }
        mockkConstructor(ClockTypeDialog::class)
        justRun { anyConstructed<ClockTypeDialog>().showNow(any(), any()) }
        mockkConstructor(AnalogClockTypeDialog::class)
        justRun { anyConstructed<AnalogClockTypeDialog>().showNow(any(), any()) }
        mockkStatic(::createNewClock)
        val mockClockView = mockk<View>()
        every { createNewClock(any(), any()) } returns mockClockView
        justRun { mockClockView.isClickable = any() }

        setupClockPreview(context, corePrefsRepo, fragmentManager)(binding)

        verify(exactly = 1) { clockWrapper.setOnClickListener(clickListenerSlot.captured) }
        verify(exactly = 1) { clockOptionsButton.setOnClickListener(optionsClickListenerSlot.captured) }
        // Initial observe triggers with default ClockType.none
        verify(exactly = 1) { clockWrapper.removeAllViews() }
        verify(exactly = 1) { clockWrapper.setBackgroundResource(R.drawable.imageview_border) }
        verify(exactly = 1) { clockOptionsButton.visibility = View.GONE }

        // Simulate tapping the clock wrapper to open dialog
        clickListenerSlot.captured.onClick(clockWrapper)
        verify(exactly = 1) { anyConstructed<ClockTypeDialog>().showNow(fragmentManager, null) }

        // Simulate changing clock type to analog - should update the view and show options button
        corePrefsRepo.updateAsync(setClockType(ClockType.analog))
        verify(exactly = 2) { clockWrapper.removeAllViews() }
        verify(exactly = 1) { clockWrapper.setBackgroundResource(0) }
        verify(exactly = 1) { clockWrapper.addView(any()) }
        verify(exactly = 1) { clockOptionsButton.visibility = View.VISIBLE }

        // Simulate tapping the options button to open analog clock type dialog
        optionsClickListenerSlot.captured.onClick(clockOptionsButton)
        verify(exactly = 1) { anyConstructed<AnalogClockTypeDialog>().showNow(fragmentManager, null) }

        // Simulate changing analog clock type - should rebuild clock since we're in analog mode
        corePrefsRepo.updateAsync(setAnalogClockType(AnalogClockType.analog_4))
        verify(exactly = 3) { clockWrapper.removeAllViews() }
        verify(exactly = 2) { clockWrapper.setBackgroundResource(0) }
        verify(exactly = 2) { clockWrapper.addView(any()) }

        // Simulate changing to a different clock type (binary)
        corePrefsRepo.updateAsync(setClockType(ClockType.binary))
        verify(exactly = 4) { clockWrapper.removeAllViews() }
        verify(exactly = 3) { clockWrapper.setBackgroundResource(0) }
        verify(exactly = 3) { clockWrapper.addView(any()) }
        verify(exactly = 2) { clockOptionsButton.visibility = View.GONE }

        // Simulate setting the same clock type - should NOT update clock, but visibility is still set
        corePrefsRepo.updateAsync(setClockType(ClockType.binary))
        verify(exactly = 4) { clockWrapper.removeAllViews() }
        verify(exactly = 3) { clockWrapper.addView(any()) }
        verify(exactly = 3) { clockOptionsButton.visibility = View.GONE }

        // Simulate setting clock type to none - should show border, not add clock
        corePrefsRepo.updateAsync(setClockType(ClockType.none))
        verify(exactly = 5) { clockWrapper.removeAllViews() }
        verify(exactly = 2) { clockWrapper.setBackgroundResource(R.drawable.imageview_border) }
        verify(exactly = 3) { clockWrapper.addView(any()) } // Still only 3 addView calls
        verify(exactly = 4) { clockOptionsButton.visibility = View.GONE }
    }
}
