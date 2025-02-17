package com.jkuester.unlauncher.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton.OnCheckedChangeListener
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.jkuester.unlauncher.datasource.setDisplayInDrawer
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.R
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private val app0 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App 0")
    .setDisplayInDrawer(true)
    .build()
private val app1 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App 1")
    .setDisplayInDrawer(false)
    .build()
private val apps = UnlauncherApps
    .newBuilder()
    .addApps(app0)
    .addApps(app1)
    .build()

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CustomizeAppDrawerVisibleAppsAdapterTest {
    @MockK
    lateinit var mAppsRepo: UnlauncherAppsRepository

    private lateinit var adapter: CustomizeAppDrawerVisibleAppsAdapter

    @BeforeEach
    fun beforeEach() {
        every { mAppsRepo.get() } returns apps
        adapter = CustomizeAppDrawerVisibleAppsAdapter(mAppsRepo)
    }

    @AfterEach
    fun afterEach() {
        verify(exactly = 1) { mAppsRepo.get() }
    }

    @Test
    fun getItemCount() {
        val count = adapter.itemCount
        count shouldBe 2
    }

    @Test
    fun onBindViewHolder() {
        val viewHolder = mockk<CustomizeAppDrawerVisibleAppsAdapter.ViewHolder>()
        val appName = mockk<CheckBox>()
        every { viewHolder.appName } returns appName
        justRun { appName.text = any() }
        justRun { appName.isChecked = any() }
        val onCheckedSlot = slot<OnCheckedChangeListener>()
        justRun { appName.setOnCheckedChangeListener(capture(onCheckedSlot)) }
        val mockUpdatePrefs = mockk<(UnlauncherApps) -> UnlauncherApps>()
        mockkStatic(::setDisplayInDrawer)
        every { setDisplayInDrawer(any(), any()) } returns mockUpdatePrefs
        every { mAppsRepo.updateAsync(any()) } returns mockk()

        adapter.onBindViewHolder(viewHolder, 0)

        verify(exactly = 2) { viewHolder.appName }
        verify(exactly = 1) { appName.text = "App 0" }
        verify(exactly = 1) { appName.isChecked = true }
        verify(exactly = 1) { appName.setOnCheckedChangeListener(onCheckedSlot.captured) }

        onCheckedSlot.captured.onCheckedChanged(appName, false)

        verify(exactly = 1) { setDisplayInDrawer(app0, false) }
        verify(exactly = 1) { mAppsRepo.updateAsync(mockUpdatePrefs) }
    }

    @Test
    fun onCreateViewHolder() {
        val parent = mockk<ViewGroup>()
        val context = mockk<Context>()
        every { parent.context } returns context
        mockkStatic(LayoutInflater::from)
        val layoutInflater = mockk<LayoutInflater>()
        every { LayoutInflater.from(any()) } returns layoutInflater
        val view = mockk<View>()
        every { layoutInflater.inflate(any<Int>(), parent, false) } returns view
        val checkBox = mockk<CheckBox>()
        every { view.findViewById<CheckBox>(any()) } returns checkBox

        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.appName shouldBe checkBox
        verify(exactly = 1) { parent.context }
        verify(exactly = 1) { LayoutInflater.from(context) }
        verify(exactly = 1) {
            layoutInflater.inflate(R.layout.customize_app_drawer_visible_apps_list_item, parent, false)
        }
        verify(exactly = 1) { view.findViewById<CheckBox>(R.id.customize_app_drawer_fragment_app_list_item) }
    }
}
