package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datasource.setSearchBarPosition
import com.jkuester.unlauncher.datasource.setShowSearchBar
import com.jkuester.unlauncher.datasource.toggleActivateKeyboardInDrawer
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerBinding
import io.kotest.matchers.shouldBe
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
// @MockKExtension.ConfirmVerification Weird bug between mockk/kotlin/Java is causing this to fail
@ExtendWith(MockKExtension::class)
class CustomizeAppDrawerBindingsTest {
    @MockK
    lateinit var rootView: ConstraintLayout
    @MockK
    lateinit var headerBack: ImageView
    @MockK
    lateinit var headerTitle: TextView
    @MockK
    lateinit var visibleApps: TextView
    @MockK
    lateinit var searchFieldOptionsTitle: TextView
    @MockK
    lateinit var searchFieldOptionsSubtitle: TextView
    @MockK
    lateinit var showHeadingsTitle: TextView
    @MockK
    lateinit var showHeadingsSubtitle: TextView
    @MockK
    lateinit var showHeadingsSwitch: SwitchCompat

    private lateinit var binding: CustomizeAppDrawerBinding
    private val prefsRepo = TestDataRepository(CorePreferences.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_back) } returns headerBack
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_title) } returns headerTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.visible_apps) } returns visibleApps
        every { ViewBindings.findChildViewById<View>(any(), R.id.search_field_options_title) } returns
            searchFieldOptionsTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.search_field_options_subtitle) } returns
            searchFieldOptionsSubtitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.show_headings_switch_title) } returns showHeadingsTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.show_headings_switch_subtitle) } returns
            showHeadingsSubtitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.show_headings_switch_toggle) } returns
            showHeadingsSwitch

        binding = CustomizeAppDrawerBinding.bind(rootView)
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_back) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.visible_apps) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_field_options_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_field_options_subtitle) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.show_headings_switch_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.show_headings_switch_subtitle) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.show_headings_switch_toggle) }
    }

    @Test
    fun setupCustomizeAppDrawerBackButton() {
        val activity = mockk<FragmentActivity>()
        val onBackPressedDispatcher = mockk<OnBackPressedDispatcher>()
        every { activity.onBackPressedDispatcher } returns onBackPressedDispatcher
        justRun { onBackPressedDispatcher.onBackPressed() }
        val clickListenerSlot = slot<OnClickListener>()
        justRun { headerBack.setOnClickListener(capture(clickListenerSlot)) }

        setupCustomizeAppDrawerBackButton(activity)(binding)

        verify(exactly = 1) { headerBack.setOnClickListener(clickListenerSlot.captured) }

        clickListenerSlot.captured.onClick(headerBack)

        verify(exactly = 1) { activity.onBackPressedDispatcher }
        verify(exactly = 1) { onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun setupShowHeadingSwitch() {
        val titleClickListenerSlot = slot<OnClickListener>()
        justRun { showHeadingsTitle.setOnClickListener(capture(titleClickListenerSlot)) }
        val subTitleClickListenerSlot = slot<OnClickListener>()
        justRun { showHeadingsSubtitle.setOnClickListener(capture(subTitleClickListenerSlot)) }
        val switchClickListenerSlot = slot<OnClickListener>()
        justRun { showHeadingsSwitch.setOnClickListener(capture(switchClickListenerSlot)) }
        justRun { showHeadingsSwitch.isChecked = any() }

        setupShowHeadingSwitch(prefsRepo)(binding)

        verify(exactly = 1) { showHeadingsTitle.setOnClickListener(titleClickListenerSlot.captured) }
        verify(exactly = 1) { showHeadingsSubtitle.setOnClickListener(subTitleClickListenerSlot.captured) }
        verify(exactly = 1) { showHeadingsSwitch.setOnClickListener(switchClickListenerSlot.captured) }
        verify(exactly = 1) { showHeadingsSwitch.isChecked = false }
        prefsRepo.get().showDrawerHeadings shouldBe false

        titleClickListenerSlot.captured.onClick(showHeadingsTitle)
        verify(exactly = 1) { showHeadingsSwitch.isChecked = true }
        prefsRepo.get().showDrawerHeadings shouldBe true

        subTitleClickListenerSlot.captured.onClick(showHeadingsSubtitle)
        verify(exactly = 2) { showHeadingsSwitch.isChecked = false }
        prefsRepo.get().showDrawerHeadings shouldBe false

        switchClickListenerSlot.captured.onClick(showHeadingsSwitch)
        verify(exactly = 2) { showHeadingsSwitch.isChecked = true }
        prefsRepo.get().showDrawerHeadings shouldBe true
    }

    @Test
    fun setupVisibleAppsButton() {
        mockkStatic(Navigation::class)
        val navigatorClickListener = mockk<OnClickListener>()
        every { Navigation.createNavigateOnClickListener(any<Int>()) } returns navigatorClickListener
        justRun { visibleApps.setOnClickListener(any()) }

        setupVisibleAppsButton(binding)

        verify(exactly = 1) {
            Navigation.createNavigateOnClickListener(
                R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment
            )
        }
        verify(exactly = 1) { visibleApps.setOnClickListener(navigatorClickListener) }
    }

    @Test
    fun setupSearchFieldOptionsButton() {
        val resources = mockk<Resources>()
        every { resources.getStringArray(any()) } returns arrayOf("Top", "Bottom")
        every { resources.getString(any(), any(), any()) } returns "subtitle"
        every { resources.getText(any()) } returns "keyboard text"

        justRun { searchFieldOptionsTitle.setOnClickListener(any()) }
        justRun { searchFieldOptionsSubtitle.setOnClickListener(any()) }
        justRun { searchFieldOptionsSubtitle.text = any() }
        mockkStatic(Navigation::class)
        val navigatorClickListener = mockk<OnClickListener>()
        every { Navigation.createNavigateOnClickListener(any<Int>()) } returns navigatorClickListener

        setupSearchFieldOptionsButton(prefsRepo, resources)(binding)

        verify(exactly = 1) {
            Navigation.createNavigateOnClickListener(
                R.id.action_customiseAppDrawerFragment_to_customizeSearchFieldFragment
            )
        }
        verify(exactly = 1) { searchFieldOptionsTitle.setOnClickListener(navigatorClickListener) }
        verify(exactly = 1) { searchFieldOptionsSubtitle.setOnClickListener(navigatorClickListener) }
        verify(exactly = 1) { resources.getStringArray(R.array.search_bar_position_array) }
        verify(exactly = 1) { resources.getText(R.string.hidden) }
        verify(exactly = 1) {
            resources.getString(
                R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_shown,
                "top",
                "keyboard text"
            )
        }
        verify(exactly = 1) { searchFieldOptionsSubtitle.text = "subtitle" }

        // Simulate changes to watched properties:

        prefsRepo.updateAsync(setShowSearchBar(true))
        verify(exactly = 2) {
            resources.getString(
                R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_shown,
                "top",
                "keyboard text"
            )
        }

        prefsRepo.updateAsync(setSearchBarPosition(SearchBarPosition.bottom))
        verify(exactly = 1) {
            resources.getString(
                R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_shown,
                "bottom",
                "keyboard text"
            )
        }

        prefsRepo.updateAsync(toggleActivateKeyboardInDrawer())
        verify(exactly = 1) { resources.getText(R.string.shown) }

        prefsRepo.updateAsync(setShowSearchBar(false))
        verify(exactly = 1) {
            resources.getText(R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_hidden)
        }
    }
}
