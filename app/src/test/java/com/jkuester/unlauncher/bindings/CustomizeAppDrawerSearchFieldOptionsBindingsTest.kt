package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import android.view.View
import android.view.View.OnClickListener
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBindings
import com.jkuester.unlauncher.datasource.setShowSearchBar
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.dialog.SearchBarPositionDialog
import com.jkuester.unlauncher.util.TestDataRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerSearchFieldOptionsBinding
import io.kotest.matchers.shouldBe
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
class CustomizeAppDrawerSearchFieldOptionsBindingsTest {
    @MockK
    lateinit var rootView: ConstraintLayout
    @MockK
    lateinit var headerBack: ImageView
    @MockK
    lateinit var headerTitle: TextView
    @MockK
    lateinit var showSearchFieldSwitch: SwitchCompat
    @MockK
    lateinit var searchFieldPositionTitle: TextView
    @MockK
    lateinit var searchFieldPositionSubtitle: TextView
    @MockK
    lateinit var openKeyboardSwitchTitle: TextView
    @MockK
    lateinit var openKeyboardSwitchSubtitle: TextView
    @MockK
    lateinit var openKeyboardSwitchToggle: SwitchCompat
    @MockK
    lateinit var searchAllSwitchTitle: TextView
    @MockK
    lateinit var searchAllSwitchSubtitle: TextView
    @MockK
    lateinit var searchAllSwitchToggle: SwitchCompat

    private lateinit var optionsBinding: CustomizeAppDrawerSearchFieldOptionsBinding
    private val prefsRepo = TestDataRepository(CorePreferences.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        val function: KFunction<View?> = ViewBindings::findChildViewById
        mockkStatic(function)
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_back) } returns headerBack
        every { ViewBindings.findChildViewById<View>(any(), R.id.header_title) } returns headerTitle
        every { ViewBindings.findChildViewById<View>(any(), R.id.show_search_field_switch) } returns
            showSearchFieldSwitch
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.search_field_position_title)
        } returns searchFieldPositionTitle
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.search_field_position_subtitle)
        } returns searchFieldPositionSubtitle
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.open_keyboard_switch_title)
        } returns openKeyboardSwitchTitle
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.open_keyboard_switch_subtitle)
        } returns openKeyboardSwitchSubtitle
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.open_keyboard_switch_toggle)
        } returns openKeyboardSwitchToggle
        every { ViewBindings.findChildViewById<View>(any(), R.id.search_all_switch_title) } returns searchAllSwitchTitle
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.search_all_switch_subtitle)
        } returns searchAllSwitchSubtitle
        every {
            ViewBindings.findChildViewById<View>(any(), R.id.search_all_switch_toggle)
        } returns searchAllSwitchToggle

        optionsBinding = CustomizeAppDrawerSearchFieldOptionsBinding.bind(rootView)
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_back) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.header_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.show_search_field_switch) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_field_position_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_field_position_subtitle) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.open_keyboard_switch_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.open_keyboard_switch_subtitle) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.open_keyboard_switch_toggle) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_all_switch_title) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_all_switch_subtitle) }
        verify(exactly = 1) { ViewBindings.findChildViewById<View>(rootView, R.id.search_all_switch_toggle) }
    }

    @Test
    fun setupBackButton() {
        val activity = mockk<FragmentActivity>()
        val onBackPressedDispatcher = mockk<OnBackPressedDispatcher>()
        every { activity.onBackPressedDispatcher } returns onBackPressedDispatcher
        justRun { onBackPressedDispatcher.onBackPressed() }
        val clickListenerSlot = slot<OnClickListener>()
        justRun { headerBack.setOnClickListener(capture(clickListenerSlot)) }

        setupBackButton(activity)(optionsBinding)

        verify(exactly = 1) { headerBack.setOnClickListener(clickListenerSlot.captured) }

        clickListenerSlot.captured.onClick(headerBack)

        verify(exactly = 1) { activity.onBackPressedDispatcher }
        verify(exactly = 1) { onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun setupShowSearchBarSwitch() {
        val clickListenerSlot = slot<OnCheckedChangeListener>()
        justRun { showSearchFieldSwitch.setOnCheckedChangeListener(capture(clickListenerSlot)) }
        justRun { showSearchFieldSwitch.isChecked = any() }

        setupShowSearchBarSwitch(prefsRepo)(optionsBinding)

        verify(exactly = 1) { showSearchFieldSwitch.setOnCheckedChangeListener(clickListenerSlot.captured) }
        verify(exactly = 1) { showSearchFieldSwitch.isChecked = false }

        clickListenerSlot.captured.onCheckedChanged(showSearchFieldSwitch, true)

        verify(exactly = 1) { showSearchFieldSwitch.isChecked = true }
        prefsRepo.get().showSearchBar shouldBe true
    }

    @Test
    fun setupSearchBarPositionOption() {
        val titleClickListenerSlot = slot<OnClickListener>()
        justRun { searchFieldPositionTitle.setOnClickListener(capture(titleClickListenerSlot)) }
        justRun { searchFieldPositionTitle.isEnabled = any() }
        val subtitleClickListenerSlot = slot<OnClickListener>()
        justRun { searchFieldPositionSubtitle.setOnClickListener(capture(subtitleClickListenerSlot)) }
        justRun { searchFieldPositionSubtitle.isEnabled = any() }
        justRun { searchFieldPositionSubtitle.text = any() }
        mockkConstructor(SearchBarPositionDialog::class)
        justRun { anyConstructed<SearchBarPositionDialog>().showNow(any(), any()) }
        val fragmentManager = mockk<FragmentManager>()
        val resources = mockk<Resources>()
        every { resources.getTextArray(any()) } returns arrayOf("Hello World")

        setupSearchBarPositionOption(prefsRepo, fragmentManager, resources)(optionsBinding)

        verify(exactly = 1) { searchFieldPositionTitle.setOnClickListener(titleClickListenerSlot.captured) }
        verify(exactly = 1) { searchFieldPositionSubtitle.setOnClickListener(subtitleClickListenerSlot.captured) }
        verify(exactly = 1) { resources.getTextArray(R.array.search_bar_position_array) }

        titleClickListenerSlot.captured.onClick(searchFieldPositionTitle)
        subtitleClickListenerSlot.captured.onClick(searchFieldPositionSubtitle)

        verify(exactly = 2) { SearchBarPositionDialog().showNow(fragmentManager, null) }

        verify(exactly = 1) { searchFieldPositionTitle.isEnabled = false }
        verify(exactly = 1) { searchFieldPositionSubtitle.isEnabled = false }
        verify(exactly = 1) { searchFieldPositionSubtitle.text = "Hello World" }
    }

    @Test
    fun setupKeyboardSwitch() {
        prefsRepo.updateAsync(setShowSearchBar(true))
        val titleClickListenerSlot = slot<OnClickListener>()
        justRun { openKeyboardSwitchTitle.setOnClickListener(capture(titleClickListenerSlot)) }
        justRun { openKeyboardSwitchTitle.isEnabled = any() }
        val subtitleClickListenerSlot = slot<OnClickListener>()
        justRun { openKeyboardSwitchSubtitle.setOnClickListener(capture(subtitleClickListenerSlot)) }
        justRun { openKeyboardSwitchSubtitle.isEnabled = any() }
        val toggleClickListenerSlot = slot<OnClickListener>()
        justRun { openKeyboardSwitchToggle.setOnClickListener(capture(toggleClickListenerSlot)) }
        justRun { openKeyboardSwitchToggle.isEnabled = any() }
        justRun { openKeyboardSwitchToggle.isChecked = any() }

        setupKeyboardSwitch(prefsRepo)(optionsBinding)

        verify(exactly = 1) { openKeyboardSwitchTitle.setOnClickListener(titleClickListenerSlot.captured) }
        verify(exactly = 1) { openKeyboardSwitchSubtitle.setOnClickListener(subtitleClickListenerSlot.captured) }
        verify(exactly = 1) { openKeyboardSwitchToggle.setOnClickListener(toggleClickListenerSlot.captured) }

        verify(exactly = 1) { openKeyboardSwitchTitle.isEnabled = true }
        verify(exactly = 1) { openKeyboardSwitchSubtitle.isEnabled = true }
        verify(exactly = 1) { openKeyboardSwitchToggle.isEnabled = true }
        verify(exactly = 1) { openKeyboardSwitchToggle.isChecked = false }
        prefsRepo.get().activateKeyboardInDrawer shouldBe false

        titleClickListenerSlot.captured.onClick(openKeyboardSwitchTitle)
        verify(exactly = 1) { openKeyboardSwitchToggle.isChecked = true }
        prefsRepo.get().activateKeyboardInDrawer shouldBe true

        subtitleClickListenerSlot.captured.onClick(openKeyboardSwitchSubtitle)
        verify(exactly = 2) { openKeyboardSwitchToggle.isChecked = false }
        prefsRepo.get().activateKeyboardInDrawer shouldBe false

        toggleClickListenerSlot.captured.onClick(openKeyboardSwitchToggle)
        verify(exactly = 2) { openKeyboardSwitchToggle.isChecked = true }
        prefsRepo.get().activateKeyboardInDrawer shouldBe true
    }

    @Test
    fun setupSearchAllAppsSwitch() {
        val titleClickListenerSlot = slot<OnClickListener>()
        justRun { searchAllSwitchTitle.setOnClickListener(capture(titleClickListenerSlot)) }
        justRun { searchAllSwitchTitle.isEnabled = any() }
        val subtitleClickListenerSlot = slot<OnClickListener>()
        justRun { searchAllSwitchSubtitle.setOnClickListener(capture(subtitleClickListenerSlot)) }
        justRun { searchAllSwitchSubtitle.isEnabled = any() }
        val toggleClickListenerSlot = slot<OnClickListener>()
        justRun { searchAllSwitchToggle.setOnClickListener(capture(toggleClickListenerSlot)) }
        justRun { searchAllSwitchToggle.isEnabled = any() }
        justRun { searchAllSwitchToggle.isChecked = any() }

        setupSearchAllAppsSwitch(prefsRepo)(optionsBinding)

        verify(exactly = 1) { searchAllSwitchTitle.setOnClickListener(titleClickListenerSlot.captured) }
        verify(exactly = 1) { searchAllSwitchSubtitle.setOnClickListener(subtitleClickListenerSlot.captured) }
        verify(exactly = 1) { searchAllSwitchToggle.setOnClickListener(toggleClickListenerSlot.captured) }

        verify(exactly = 1) { searchAllSwitchTitle.isEnabled = false }
        verify(exactly = 1) { searchAllSwitchSubtitle.isEnabled = false }
        verify(exactly = 1) { searchAllSwitchToggle.isEnabled = false }
        verify(exactly = 1) { searchAllSwitchToggle.isChecked = false }
        prefsRepo.get().searchAllAppsInDrawer shouldBe false

        titleClickListenerSlot.captured.onClick(searchAllSwitchTitle)
        verify(exactly = 1) { searchAllSwitchToggle.isChecked = true }
        prefsRepo.get().searchAllAppsInDrawer shouldBe true

        subtitleClickListenerSlot.captured.onClick(searchAllSwitchSubtitle)
        verify(exactly = 2) { searchAllSwitchToggle.isChecked = false }
        prefsRepo.get().searchAllAppsInDrawer shouldBe false

        toggleClickListenerSlot.captured.onClick(searchAllSwitchToggle)
        verify(exactly = 2) { searchAllSwitchToggle.isChecked = true }
        prefsRepo.get().searchAllAppsInDrawer shouldBe true
    }
}
