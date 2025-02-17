package com.jkuester.unlauncher.fragment

import android.content.res.Resources
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.jkuester.unlauncher.bindings.setupCustomizeAppDrawerBackButton
import com.jkuester.unlauncher.bindings.setupSearchFieldOptionsButton
import com.jkuester.unlauncher.bindings.setupShowHeadingSwitch
import com.jkuester.unlauncher.bindings.setupVisibleAppsButton
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerBinding
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
class CustomizeAppDrawerFragmentTest {
    @MockK
    lateinit var mActivity: ComponentActivity
    @MockK
    lateinit var mResources: Resources
    @MockK
    lateinit var prefsRepo: DataRepository<CorePreferences>
    @MockK
    lateinit var view: ConstraintLayout

    private lateinit var fragment: CustomizeAppDrawerFragment

    @BeforeEach
    fun beforeEach() {
        fragment = CustomizeAppDrawerFragment()
            .apply {
                corePreferencesRepo = prefsRepo
                iActivity = mActivity
                iResources = mResources
            }
    }

    @Test
    fun onCreateView() {
        val inflater = mockk<LayoutInflater>()
        every { inflater.inflate(any<Int>(), any(), any()) } returns view

        val result = fragment.onCreateView(inflater, null, null)

        result shouldBe view
        verify(exactly = 1) { inflater.inflate(R.layout.customize_app_drawer, null, false) }
    }

    @Test
    fun onViewCreated() {
        val options = mockk<CustomizeAppDrawerBinding>()
        mockkStatic(CustomizeAppDrawerBinding::bind)
        every { CustomizeAppDrawerBinding.bind(any()) } returns options
        val optionConsumer = mockk<(t: CustomizeAppDrawerBinding) -> Unit>()
        justRun { optionConsumer(any()) }
        mockkStatic(
            ::setupVisibleAppsButton,
            ::setupCustomizeAppDrawerBackButton,
            ::setupSearchFieldOptionsButton,
            ::setupShowHeadingSwitch,
        )
        every { setupVisibleAppsButton(any()) } returns mockk()
        every { setupCustomizeAppDrawerBackButton(any()) } returns optionConsumer
        every { setupSearchFieldOptionsButton(any(), any()) } returns optionConsumer
        every { setupShowHeadingSwitch(any()) } returns optionConsumer

        fragment.onViewCreated(view, null)

        verify(exactly = 1) { CustomizeAppDrawerBinding.bind(view) }
        verify(exactly = 1) { setupVisibleAppsButton(options) }
        verify(exactly = 1) { setupCustomizeAppDrawerBackButton(mActivity) }
        verify(exactly = 1) { setupSearchFieldOptionsButton(prefsRepo, mResources) }
        verify(exactly = 1) { setupShowHeadingSwitch(prefsRepo) }
        verify(exactly = 3) { optionConsumer(options) }
    }
}
