package com.jkuester.unlauncher.fragment

import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.jkuester.unlauncher.bindings.setupAddAppBackButton
import com.jkuester.unlauncher.bindings.setupAddAppsList
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeHomeAppsAddAppBinding
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
class CustomizeHomeAppsAddAppFragmentTest {
    @MockK
    lateinit var mActivity: ComponentActivity
    @MockK
    lateinit var appsRepo: DataRepository<UnlauncherApps>
    @MockK
    lateinit var view: ConstraintLayout

    private lateinit var fragment: CustomizeHomeAppsAddAppFragment

    @BeforeEach
    fun beforeEach() {
        fragment = CustomizeHomeAppsAddAppFragment()
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
        verify(exactly = 1) { inflater.inflate(R.layout.customize_home_apps_add_app, null, false) }
    }

    @Test
    fun onViewCreated() {
        val options = mockk<CustomizeHomeAppsAddAppBinding>()
        mockkStatic(CustomizeHomeAppsAddAppBinding::bind)
        every { CustomizeHomeAppsAddAppBinding.bind(any()) } returns options
        val optionConsumer = mockk<(t: CustomizeHomeAppsAddAppBinding) -> Unit>()
        justRun { optionConsumer(any()) }
        mockkStatic(
            ::setupAddAppBackButton,
            ::setupAddAppsList
        )
        every { setupAddAppBackButton(any()) } returns optionConsumer
        every { setupAddAppsList(any(), any()) } returns optionConsumer

        fragment.onViewCreated(view, null)

        verify(exactly = 1) { CustomizeHomeAppsAddAppBinding.bind(view) }
        verify(exactly = 1) { setupAddAppBackButton(mActivity) }
        verify(exactly = 1) { setupAddAppsList(appsRepo, mActivity) }
        verify(exactly = 2) { optionConsumer(options) }
    }
}
