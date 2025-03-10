package com.jkuester.unlauncher.adapter

import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private val unlauncherApp0 = UnlauncherApp
    .newBuilder()
    .setPackageName("packageName0")
    .setClassName("activityName0")
    .setDisplayName("appName0")
    .build()
private val unlauncherApp1 = UnlauncherApp
    .newBuilder()
    .setPackageName("packageName1")
    .setClassName("activityName1")
    .setDisplayName("appName1")
    .build()
private val unlauncherApp2 = UnlauncherApp
    .newBuilder()
    .setPackageName("packageName2")
    .setClassName("activityName2")
    .setDisplayName("appName2")
    .build()
@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class AdapterNotifiersTest {
    @Test
    fun notifyOfHomeAppChanges_homeAppsNotChanged() {
        val homeApp = unlauncherApp0.toBuilder().setHomeAppIndex(0).build()
        val updatedData = UnlauncherApps.newBuilder().addApps(homeApp).build()

        val adapter = mockk<CustomizeHomeAppsListAdapter>()
        every { adapter.apps } returns listOf(homeApp)

        notifyOfHomeAppChanges(adapter).onChanged(updatedData)

        verify(exactly = 1) { adapter.apps }
    }

    @Test
    fun notifyOfHomeAppChanges_addHomeApp() {
        val homeApp = unlauncherApp0.toBuilder().setHomeAppIndex(0).build()
        val updatedData = UnlauncherApps.newBuilder().addApps(homeApp).build()

        val adapter = mockk<CustomizeHomeAppsListAdapter>()
        every { adapter.apps } returns emptyList()
        justRun { adapter.apps = any() }
        justRun { adapter.notifyItemInserted(any()) }

        notifyOfHomeAppChanges(adapter).onChanged(updatedData)

        verify(exactly = 1) { adapter.apps }
        verify(exactly = 1) { adapter.apps = listOf(homeApp) }
        verify(exactly = 1) { adapter.notifyItemInserted(0) }
    }

    @Test
    fun notifyOfHomeAppChanges_RemoveHomeApp() {
        val homeApp = unlauncherApp0.toBuilder().setHomeAppIndex(0).build()
        val updatedData = UnlauncherApps.newBuilder().addApps(unlauncherApp0).build()

        val adapter = mockk<CustomizeHomeAppsListAdapter>()
        every { adapter.apps } returns listOf(homeApp)
        justRun { adapter.apps = any() }
        justRun { adapter.notifyItemRemoved(any()) }

        notifyOfHomeAppChanges(adapter).onChanged(updatedData)

        verify(exactly = 1) { adapter.apps }
        verify(exactly = 1) { adapter.apps = emptyList() }
        verify(exactly = 1) { adapter.notifyItemRemoved(0) }
    }

    @Test
    fun notifyOfHomeAppChanges_displayNameChanged() {
        val homeApp = unlauncherApp0.toBuilder().setHomeAppIndex(0).build()
        val updatedHomeApp = homeApp.toBuilder().setDisplayName("new name").build()
        val updatedData = UnlauncherApps.newBuilder().addApps(updatedHomeApp).build()

        val adapter = mockk<CustomizeHomeAppsListAdapter>()
        every { adapter.apps } returns listOf(homeApp)
        justRun { adapter.apps = any() }
        justRun { adapter.notifyItemChanged(any()) }

        notifyOfHomeAppChanges(adapter).onChanged(updatedData)

        verify(exactly = 1) { adapter.apps }
        verify(exactly = 1) { adapter.apps = listOf(updatedHomeApp) }
        verify(exactly = 1) { adapter.notifyItemChanged(0) }
    }

    @Test
    fun notifyOfHomeAppChanges_addRemoveChanged() {
        val homeApp0 = unlauncherApp0.toBuilder().setHomeAppIndex(0).build()
        val homeApp1 = unlauncherApp1.toBuilder().setHomeAppIndex(1).build()
        val updatedHomeApp1 = unlauncherApp1.toBuilder().setHomeAppIndex(0).setDisplayName("updated Name").build()
        val homeApp2 = unlauncherApp2.toBuilder().setHomeAppIndex(1).build()
        val updatedData = UnlauncherApps.newBuilder().addAllApps(
            listOf(unlauncherApp0, updatedHomeApp1, homeApp2)
        ).build()

        val adapter = mockk<CustomizeHomeAppsListAdapter>()
        every { adapter.apps } returns listOf(homeApp0, homeApp1)
        justRun { adapter.apps = any() }
        justRun { adapter.notifyItemRemoved(any()) }
        justRun { adapter.notifyItemInserted(any()) }
        justRun { adapter.notifyItemChanged(any()) }

        notifyOfHomeAppChanges(adapter).onChanged(updatedData)

        verify(exactly = 1) { adapter.apps }
        verify(exactly = 1) { adapter.apps = listOf(updatedHomeApp1, homeApp2) }
        verify(exactly = 1) { adapter.notifyItemRemoved(0) }
        verify(exactly = 1) { adapter.notifyItemInserted(1) }
        verify(exactly = 1) { adapter.notifyItemChanged(0) }
    }

    @Test
    fun notifyOfHomeAppChanges_moveHomeApp() {
        val homeApp0 = unlauncherApp0.toBuilder().setHomeAppIndex(0).build()
        val homeApp1 = unlauncherApp1.toBuilder().setHomeAppIndex(1).build()
        val updatedHomeApp0 = unlauncherApp0.toBuilder().setHomeAppIndex(1).build()
        val updatedHomeApp1 = unlauncherApp1.toBuilder().setHomeAppIndex(0).build()
        val updatedData = UnlauncherApps.newBuilder().addAllApps(listOf(updatedHomeApp0, updatedHomeApp1)).build()

        val adapter = mockk<CustomizeHomeAppsListAdapter>()
        every { adapter.apps } returns listOf(homeApp0, homeApp1)
        justRun { adapter.apps = any() }
        justRun { adapter.notifyItemMoved(any(), any()) }

        notifyOfHomeAppChanges(adapter).onChanged(updatedData)

        verify(exactly = 1) { adapter.apps }
        verify(exactly = 1) { adapter.apps = listOf(updatedHomeApp1, updatedHomeApp0) }
        verify(exactly = 1) { adapter.notifyItemMoved(0, 1) }
    }
}
