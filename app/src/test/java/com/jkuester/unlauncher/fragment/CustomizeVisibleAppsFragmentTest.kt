package com.jkuester.unlauncher.fragment

import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.jkuester.unlauncher.bindings.setupVisibleAppsBackButton
import com.jkuester.unlauncher.bindings.setupVisibleAppsList
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerVisibleAppsBinding
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
class CustomizeVisibleAppsFragmentTest {
    @MockK
    lateinit var mActivity: ComponentActivity
    @MockK
    lateinit var appsRepo: UnlauncherAppsRepository
    @MockK
    lateinit var view: ConstraintLayout

    private lateinit var fragment: CustomizeVisibleAppsFragment

    @BeforeEach
    fun beforeEach() {
        fragment = CustomizeVisibleAppsFragment()
            .apply {
                unlauncherAppsRepo = appsRepo
                iActivity = mActivity
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
                R.layout.customize_app_drawer_visible_apps,
                null,
                false
            )
        }
    }

    @Test
    fun onViewCreated() {
        val options = mockk<CustomizeAppDrawerVisibleAppsBinding>()
        mockkStatic(CustomizeAppDrawerVisibleAppsBinding::bind)
        every { CustomizeAppDrawerVisibleAppsBinding.bind(any()) } returns options
        val optionConsumer = mockk<(t: CustomizeAppDrawerVisibleAppsBinding) -> Unit>()
        justRun { optionConsumer(any()) }
        mockkStatic(::setupVisibleAppsBackButton, ::setupVisibleAppsList)
        every { setupVisibleAppsBackButton(any()) } returns optionConsumer
        every { setupVisibleAppsList(any()) } returns optionConsumer

        fragment.onViewCreated(view, null)

        verify(exactly = 1) { CustomizeAppDrawerVisibleAppsBinding.bind(view) }
        verify(exactly = 1) { setupVisibleAppsBackButton(mActivity) }
        verify(exactly = 1) { setupVisibleAppsList(appsRepo) }
        verify(exactly = 2) { optionConsumer(options) }
    }
}
