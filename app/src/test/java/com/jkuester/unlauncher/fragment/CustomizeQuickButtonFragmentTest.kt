package com.jkuester.unlauncher.fragment

import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.bindings.setupAddHomeAppButton
import com.jkuester.unlauncher.bindings.setupCustomizeQuickButtonsBackButton
import com.jkuester.unlauncher.bindings.setupHomeAppsList
import com.jkuester.unlauncher.bindings.setupQuickButtonIcons
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsBinding
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
class CustomizeQuickButtonFragmentTest {
    @MockK
    lateinit var mActivity: ComponentActivity
    @MockK
    lateinit var mFragmentManager: FragmentManager
    @MockK
    lateinit var prefsRepo: DataRepository<QuickButtonPreferences>
    @MockK
    lateinit var mAppsRepo: DataRepository<UnlauncherApps>
    @MockK
    lateinit var view: ConstraintLayout

    private lateinit var fragment: CustomizeQuickButtonsFragment

    @BeforeEach
    fun beforeEach() {
        fragment = CustomizeQuickButtonsFragment()
            .apply {
                quickButtonPreferencesRepo = prefsRepo
                iActivity = mActivity
                iFragmentManager = mFragmentManager
                appsRepo = mAppsRepo
            }
    }

    @Test
    fun onCreateView() {
        val inflater = mockk<LayoutInflater>()
        every { inflater.inflate(any<Int>(), any(), any()) } returns view

        val result = fragment.onCreateView(inflater, null, null)

        result shouldBe view
        verify(exactly = 1) { inflater.inflate(R.layout.customize_quick_buttons, null, false) }
    }

    @Test
    fun onViewCreated() {
        val options = mockk<CustomizeQuickButtonsBinding>()
        mockkStatic(CustomizeQuickButtonsBinding::bind)
        every { CustomizeQuickButtonsBinding.bind(any()) } returns options
        val optionConsumer = mockk<(t: CustomizeQuickButtonsBinding) -> Unit>()
        justRun { optionConsumer(any()) }
        mockkStatic(
            ::setupCustomizeQuickButtonsBackButton,
            ::setupQuickButtonIcons,
            ::setupAddHomeAppButton,
            ::setupHomeAppsList
        )
        every { setupCustomizeQuickButtonsBackButton(any()) } returns optionConsumer
        every { setupQuickButtonIcons(any(), any()) } returns optionConsumer
        every { setupAddHomeAppButton(any()) } returns optionConsumer
        every { setupHomeAppsList(any(), any()) } returns optionConsumer

        fragment.onViewCreated(view, null)

        verify(exactly = 1) { CustomizeQuickButtonsBinding.bind(view) }
        verify(exactly = 1) { setupCustomizeQuickButtonsBackButton(mActivity) }
        verify(exactly = 1) { setupQuickButtonIcons(prefsRepo, mFragmentManager) }
        verify(exactly = 1) { setupAddHomeAppButton(mAppsRepo) }
        verify(exactly = 1) { setupHomeAppsList(mAppsRepo, mFragmentManager) }
        verify(exactly = 4) { optionConsumer(options) }
    }
}
