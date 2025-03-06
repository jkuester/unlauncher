package com.jkuester.unlauncher.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.jkuester.unlauncher.adapter.notifyOfHomeAppChanges
import com.jkuester.unlauncher.datasource.getHomeApps
import com.jkuester.unlauncher.datasource.unlauncherAppMatches
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.util.TestDataRepository
import com.jkuester.unlauncher.widget.PopupMenuWithIcons
import com.sduduzog.slimlauncher.R
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private val app0 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App A")
    .setPackageName("a")
    .build()
private val app1 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App B")
    .setPackageName("b")
    .setHomeAppIndex(0)
    .build()
private val app2 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App C")
    .setPackageName("c")
    .setHomeAppIndex(1)
    .build()

@MockKExtension.CheckUnnecessaryStub
// @MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CustomizeHomeAppsListAdapterTest {
    @MockK
    lateinit var mFragmentManager: FragmentManager

    private val mAppsRepo = TestDataRepository(UnlauncherApps.getDefaultInstance())

    @BeforeEach
    fun beforeEach() {
        mAppsRepo.updateAsync { it.toBuilder().addAllApps(listOf(app0, app1, app2)).build() }
    }

    @Test
    fun getItemCount() {
        val adapter = CustomizeHomeAppsListAdapter(mAppsRepo, mFragmentManager)
        val count = adapter.itemCount
        count shouldBe 2
    }

    @Test
    fun onBindViewHolder() {
        val viewHolder = mockk<CustomizeHomeAppsListAdapter.ViewHolder>()
        val appName = mockk<TextView>()
        every { viewHolder.appName } returns appName
        justRun { appName.text = any() }
        val function: (CustomizeHomeAppsListAdapter) -> Observer<UnlauncherApps> = ::notifyOfHomeAppChanges
        mockkStatic(function as KFunction<*>)
        val changeObserver = mockk<Observer<UnlauncherApps>>()
        every { notifyOfHomeAppChanges<CustomizeHomeAppsListAdapter>(any()) } returns changeObserver
        justRun { changeObserver.onChanged(any()) }
        val appNameClickSlot = slot<View.OnClickListener>()
        justRun { appName.setOnClickListener(capture(appNameClickSlot)) }
        mockkConstructor(PopupMenuWithIcons::class)
        justRun { anyConstructed<PopupMenuWithIcons>().inflate(any()) }
        val menuSlot = slot<OnMenuItemClickListener>()
        justRun { anyConstructed<PopupMenuWithIcons>().setOnMenuItemClickListener(capture(menuSlot)) }
        justRun { anyConstructed<PopupMenuWithIcons>().show() }
        val menuView = mockk<View>()
        every { menuView.context } returns mockk()

        val adapter = CustomizeHomeAppsListAdapter(mAppsRepo, mFragmentManager)
        adapter.onBindViewHolder(viewHolder, 0)

        verify(exactly = 2) { viewHolder.appName }
        verify(exactly = 1) { appName.text = app1.displayName }
        verify(exactly = 1) { notifyOfHomeAppChanges(adapter) }
        verify(exactly = 1) { appName.setOnClickListener(appNameClickSlot.captured) }

        // Simulate clicking on the app name
        appNameClickSlot.captured.onClick(menuView)

        verify(exactly = 1) { menuView.context }
        verify { anyConstructed<PopupMenuWithIcons>().inflate(R.menu.customize_home_apps_menu) }
        verify { anyConstructed<PopupMenuWithIcons>().setOnMenuItemClickListener(menuSlot.captured) }
        verify { anyConstructed<PopupMenuWithIcons>().show() }

        // Simulate clicking on the move_down menu item
        menuSlot.captured.onMenuItemClick(mockk { every { itemId } returns R.id.move_down })

        adapter.apps = getHomeApps(mAppsRepo.get())
        adapter.apps.first(unlauncherAppMatches(app1)).homeAppIndex shouldBe 1

        // Simulate clicking on the move_up menu item
        menuSlot.captured.onMenuItemClick(mockk { every { itemId } returns R.id.move_up })

        adapter.apps = getHomeApps(mAppsRepo.get())
        adapter.apps.first(unlauncherAppMatches(app1)).homeAppIndex shouldBe 0

        // Simulate clicking on the remove menu item
        menuSlot.captured.onMenuItemClick(mockk { every { itemId } returns R.id.remove })

        adapter.apps = getHomeApps(mAppsRepo.get())
        adapter.apps.firstOrNull(unlauncherAppMatches(app1)) shouldBe null
        adapter.apps.size shouldBe 1

        verify(exactly = 4) { changeObserver.onChanged(any()) }
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
        val textView = mockk<TextView>()
        every { view.findViewById<TextView>(any()) } returns textView

        val adapter = CustomizeHomeAppsListAdapter(mAppsRepo, mFragmentManager)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.appName shouldBe textView
        verify(exactly = 1) { parent.context }
        verify(exactly = 1) { LayoutInflater.from(context) }
        verify(exactly = 1) { layoutInflater.inflate(R.layout.main_fragment_list_item, parent, false) }
        verify(exactly = 1) { view.findViewById<TextView>(R.id.home_fragment_list_item_app_name) }
    }
}
