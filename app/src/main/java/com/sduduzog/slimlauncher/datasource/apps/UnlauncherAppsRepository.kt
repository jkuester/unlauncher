package com.sduduzog.slimlauncher.datasource.apps

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.jkuester.unlauncher.datastore.UnlauncherApps
import com.sduduzog.slimlauncher.data.model.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class UnlauncherAppsRepository(
    private val unlauncherAppsStore: DataStore<UnlauncherApps>,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    private val unlauncherAppsFlow: Flow<UnlauncherApps> =
        unlauncherAppsStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(
                        "UnlauncherAppsRepo",
                        "Error reading Unlauncher apps.",
                        exception
                    )
                    emit(UnlauncherApps.getDefaultInstance())
                } else {
                    throw exception
                }
            }

    fun liveData(): LiveData<UnlauncherApps> {
        return unlauncherAppsFlow.asLiveData()
    }

    fun get(): UnlauncherApps {
        return runBlocking {
            unlauncherAppsFlow.first()
        }
    }

    fun getApp(packageName: String, className: String): UnlauncherApp? {
        return runBlocking {
            findApp(unlauncherAppsFlow.first(), packageName, className)
        }
    }

    fun setApps(apps: List<App>) {
        // TODO need to try and clear out any hidden apps that no longer exist
        lifecycleScope.launch {
            unlauncherAppsStore.updateData { unlauncherApps ->
                val unlauncherAppsBuilder = unlauncherApps.toBuilder()
                apps.filter { app ->
                    findApp(
                        unlauncherApps,
                        app.packageName,
                        app.activityName
                    ) == null
                }.forEach { app ->
                    unlauncherAppsBuilder.addApps(
                        UnlauncherApp.newBuilder().setPackageName(app.packageName)
                            .setClassName(app.activityName).setUserSerial(app.userSerial)
                            .setDisplayName(app.appName).setDisplayInDrawer(true)
                    )
                }
                unlauncherAppsBuilder.build()
            }
        }
    }

    private fun findApp(
        unlauncherApps: UnlauncherApps,
        packageName: String,
        className: String
    ): UnlauncherApp? {
        return unlauncherApps.appsList.firstOrNull { app ->
            packageName == app.packageName && className == app.className
        }
    }

    fun updateDisplayInDrawer(appToUpdate: UnlauncherApp, displayInDrawer: Boolean) {
        lifecycleScope.launch {
            unlauncherAppsStore.updateData { currentApps ->
                currentApps.toBuilder().setApps(
                    currentApps.appsList.indexOf(appToUpdate),
                    appToUpdate.toBuilder().setDisplayInDrawer(displayInDrawer)
                ).build()
            }
        }
    }
}