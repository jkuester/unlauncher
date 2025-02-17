package com.jkuester.unlauncher.fragment

import android.content.res.Resources
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.bindings.setupBackButton
import com.jkuester.unlauncher.bindings.setupKeyboardSwitch
import com.jkuester.unlauncher.bindings.setupSearchAllAppsSwitch
import com.jkuester.unlauncher.bindings.setupSearchBarPositionOption
import com.jkuester.unlauncher.bindings.setupShowSearchBarSwitch
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerSearchFieldOptionsBinding
import io.kotest.matchers.ints.exactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CustomizeSearchFieldFragmentTest {
    @MockK
    lateinit var mActivity: ComponentActivity
    @MockK
    lateinit var mResources: Resources
    @MockK
    lateinit var mFragmentManager: FragmentManager
    @MockK
    lateinit var prefsRepo: CorePreferencesRepository
    @MockK
    lateinit var view: ConstraintLayout

    private lateinit var fragment: CustomizeSearchFieldFragment

    @BeforeEach
    fun beforeEach() {
        fragment = CustomizeSearchFieldFragment()
            .apply {
                corePrefsRepo = prefsRepo
                iActivity = mActivity
                iResources = mResources
                iFragmentManager = mFragmentManager
            }
    }

    @Test
    fun onCreateView() {
        val inflater = mockk<LayoutInflater>()
        every { inflater.inflate(any<Int>(), any(), any()) } returns view

        val result = fragment.onCreateView(inflater, null, null)

        result shouldBe view
        verify(exactly = 1) {
            inflater.inflate(
                R.layout.customize_app_drawer_search_field_options,
                null,
                false
            )
        }
    }

    @Test
    fun onViewCreated() {
        val options = mockk<CustomizeAppDrawerSearchFieldOptionsBinding>()
        mockkStatic(CustomizeAppDrawerSearchFieldOptionsBinding::bind)
        every { CustomizeAppDrawerSearchFieldOptionsBinding.bind(any()) } returns options
        val optionConsumer = mockk<(t: CustomizeAppDrawerSearchFieldOptionsBinding) -> Unit>()
        justRun { optionConsumer(any()) }
        mockkStatic(
            ::setupBackButton,
            ::setupShowSearchBarSwitch,
            ::setupSearchBarPositionOption,
            ::setupKeyboardSwitch,
            ::setupSearchAllAppsSwitch
        )
        every { setupBackButton(any()) } returns optionConsumer
        every { setupShowSearchBarSwitch(any()) } returns optionConsumer
        every { setupSearchBarPositionOption(any(), any(), any()) } returns optionConsumer
        every { setupKeyboardSwitch(any()) } returns optionConsumer
        every { setupSearchAllAppsSwitch(any()) } returns optionConsumer

        fragment.onViewCreated(view, null)

        verify(exactly = 1) { CustomizeAppDrawerSearchFieldOptionsBinding.bind(view) }
        verify(exactly = 1) { setupBackButton(mActivity) }
        verify(exactly = 1) { setupShowSearchBarSwitch(prefsRepo) }
        verify(exactly = 1) { setupSearchBarPositionOption(prefsRepo, mFragmentManager, mResources) }
        verify(exactly = 1) { setupKeyboardSwitch(prefsRepo) }
        verify(exactly = 1) { setupSearchAllAppsSwitch(prefsRepo) }
        verify(exactly = 5) { optionConsumer(options) }
    }
}
