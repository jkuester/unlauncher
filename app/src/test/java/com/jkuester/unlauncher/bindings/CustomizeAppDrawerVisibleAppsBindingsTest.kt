package com.jkuester.unlauncher.bindings

import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.adapter.CustomizeAppDrawerVisibleAppsAdapter
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerVisibleAppsBinding
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlin.reflect.KFunction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CustomizeAppDrawerVisibleAppsBindingsTest {
    @MockK
    lateinit var rootView: ConstraintLayout
    @MockK
    lateinit var headerBack: ImageView
    @MockK
    lateinit var headerTitle: TextView
    @MockK
    lateinit var appsList: RecyclerView

    private lateinit var optionsBinding: CustomizeAppDrawerVisibleAppsBinding

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_back) } returns headerBack
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_title) } returns headerTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.customize_app_drawer_visible_apps_list) } returns
            appsList

        optionsBinding = CustomizeAppDrawerVisibleAppsBinding.bind(rootView)
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_back) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_title) }
        verify(exactly = 1) {
            ViewBindings.findChildViewById<View>(rootView, R.id.customize_app_drawer_visible_apps_list)
        }
    }

    @Test
    fun setupVisibleAppsBackButton() {
        val activity = mockk<FragmentActivity>()
        val onBackPressedDispatcher = mockk<OnBackPressedDispatcher>()
        every { activity.onBackPressedDispatcher } returns onBackPressedDispatcher
        justRun { onBackPressedDispatcher.onBackPressed() }
        val clickListenerSlot = slot<OnClickListener>()
        justRun { headerBack.setOnClickListener(capture(clickListenerSlot)) }

        setupVisibleAppsBackButton(activity)(optionsBinding)

        verify(exactly = 1) { headerBack.setOnClickListener(clickListenerSlot.captured) }

        clickListenerSlot.captured.onClick(headerBack)

        verify(exactly = 1) { activity.onBackPressedDispatcher }
        verify(exactly = 1) { onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun setupVisibleAppsList() {
        val appsRepo = mockk<UnlauncherAppsRepository>()
        every { appsRepo.get() } returns mockk()
        justRun { appsList.adapter = any() }

        setupVisibleAppsList(appsRepo)(optionsBinding)

        verify(exactly = 1) { appsRepo.get() }
        verify(exactly = 1) { appsList.adapter = any<CustomizeAppDrawerVisibleAppsAdapter>() }
    }
}
