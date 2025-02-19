package com.jkuester.unlauncher.bindings

import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.setCenterIconId
import com.jkuester.unlauncher.datasource.setLeftIconId
import com.jkuester.unlauncher.datasource.setRightIconId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.dialog.QuickButtonIconDialog
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsBinding
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
class CustomizeQuickButtonsBindingsTest {
    @MockK
    lateinit var rootView: ConstraintLayout
    @MockK
    lateinit var headerBack: ImageView
    @MockK
    lateinit var headerTitle: TextView
    @MockK
    lateinit var quickButtonLeft: ImageView
    @MockK
    lateinit var quickButtonCenter: ImageView
    @MockK
    lateinit var quickButtonRight: ImageView

    private lateinit var binding: CustomizeQuickButtonsBinding
    private val prefsRepo = TestDataRepository(QuickButtonPreferences.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_back) } returns headerBack
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_title) } returns headerTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.quick_button_left) } returns quickButtonLeft
        every { ViewBindings.findChildViewById<View>(any(), R.id.quick_button_center) } returns quickButtonCenter
        every { ViewBindings.findChildViewById<View>(any(), R.id.quick_button_right) } returns quickButtonRight

        binding = CustomizeQuickButtonsBinding.bind(rootView)
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_back) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.quick_button_left) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.quick_button_center) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.quick_button_right) }
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
        val fragmentManager = mockk<FragmentManager>()
        mockkConstructor(QuickButtonIconDialog::class)
        justRun { anyConstructed<QuickButtonIconDialog>().showNow(any(), any()) }

        setupQuickButtonIcons(prefsRepo, fragmentManager)(binding)

        verify(exactly = 1) { quickButtonLeft.setOnClickListener(leftButtonClickListenerSlot.captured) }
        verify(exactly = 1) { quickButtonCenter.setOnClickListener(centerButtonClickListenerSlot.captured) }
        verify(exactly = 1) { quickButtonRight.setOnClickListener(rightButtonClickListenerSlot.captured) }

        // Simulate selecting icons

        leftButtonClickListenerSlot.captured.onClick(quickButtonLeft)
        verify(exactly = 1) { QuickButtonIconDialog(QuickButtonIcon.IC_CALL.prefId).showNow(fragmentManager, null) }
        prefsRepo.updateAsync(setLeftIconId(2))
        verify(exactly = 1) { quickButtonLeft.setImageResource(QuickButtonIcon.IC_CALL.resourceId) }

        centerButtonClickListenerSlot.captured.onClick(quickButtonCenter)
        verify(exactly = 2) { QuickButtonIconDialog(QuickButtonIcon.IC_COG.prefId).showNow(fragmentManager, null) }
        prefsRepo.updateAsync(setCenterIconId(3))
        verify(exactly = 1) { quickButtonCenter.setImageResource(QuickButtonIcon.IC_COG.resourceId) }

        rightButtonClickListenerSlot.captured.onClick(quickButtonRight)
        verify(exactly = 3) {
            QuickButtonIconDialog(QuickButtonIcon.IC_PHOTO_CAMERA.prefId).showNow(fragmentManager, null)
        }
        prefsRepo.updateAsync(setRightIconId(4))
        verify(exactly = 1) { quickButtonRight.setImageResource(QuickButtonIcon.IC_PHOTO_CAMERA.resourceId) }
    }
}
