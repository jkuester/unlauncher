package com.jkuester.unlauncher.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.util.TestDataRepository
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private val app0 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App A")
    .setPackageName("a")
    .setHomeAppIndex(0)
    .build()
private val app1 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App B")
    .setPackageName("b")
    .build()
private val app2 = UnlauncherApp
    .newBuilder()
    .setDisplayName("App C")
    .setPackageName("c")
    .build()

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CustomizeHomeAppsAddAppAdapterTest {
    @MockK
    lateinit var mActivity: ComponentActivity

    private val mAppsRepo = TestDataRepository(UnlauncherApps.getDefaultInstance())
    private lateinit var adapter: CustomizeHomeAppsAddAppAdapter

    @BeforeEach
    fun beforeEach() {
        mAppsRepo.updateAsync { it.toBuilder().addAllApps(listOf(app0, app1, app2)).build() }
        adapter = CustomizeHomeAppsAddAppAdapter(mAppsRepo, mActivity)
    }

    @Test
    fun getItemCount() {
        val count = adapter.itemCount
        count shouldBe 2
    }

    @Test
    fun onBindViewHolder() {
        val viewHolder = mockk<CustomizeHomeAppsAddAppAdapter.ViewHolder>()
        val appName = mockk<TextView>()
        every { viewHolder.appName } returns appName
        justRun { appName.text = any() }
        val onClickedSlot = slot<OnClickListener>()
        justRun { appName.setOnClickListener(capture(onClickedSlot)) }
        justRun { mActivity.onBackPressedDispatcher.onBackPressed() }

        adapter.onBindViewHolder(viewHolder, 0)

        verify(exactly = 2) { viewHolder.appName }
        verify(exactly = 1) { appName.text = app1.displayName }
        verify(exactly = 1) { appName.setOnClickListener(onClickedSlot.captured) }

        onClickedSlot.captured.onClick(appName)

        verify(exactly = 1) { mActivity.onBackPressedDispatcher.onBackPressed() }
        mAppsRepo.get().appsList[1].homeAppIndex shouldBe 1
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

        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        viewHolder.appName shouldBe textView
        verify(exactly = 1) { parent.context }
        verify(exactly = 1) { LayoutInflater.from(context) }
        verify(exactly = 1) { layoutInflater.inflate(R.layout.app_list_item, parent, false) }
        verify(exactly = 1) { view.findViewById<TextView>(R.id.app_list_item_name) }
    }
}
