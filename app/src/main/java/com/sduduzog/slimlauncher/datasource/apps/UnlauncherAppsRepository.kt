package com.sduduzog.slimlauncher.datasource.apps

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.jkuester.unlauncher.datastore.UnlauncherApps
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.HomeApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

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

    suspend fun setApps(apps: List<App>) {
        unlauncherAppsStore.updateData { unlauncherApps ->
            val unlauncherAppsBuilder = unlauncherApps.toBuilder()
            // Add any new apps
            var appAdded = false
            apps.filter { app ->
                findApp(
                    unlauncherAppsBuilder.appsList,
                    app.packageName,
                    app.activityName
                ) == null
            }.forEach { app ->
                unlauncherAppsBuilder.addApps(
                    UnlauncherApp.newBuilder().setPackageName(app.packageName)
                        .setClassName(app.activityName).setUserSerial(app.userSerial)
                        .setDisplayName(app.appName).setDisplayInDrawer(true)
                )
                appAdded = true
            }
            // Remove any apps that no longer exist
            unlauncherApps.appsList.filter { unlauncherApp ->
                apps.find { app ->
                    unlauncherApp.packageName == app.packageName && unlauncherApp.className == app.activityName
                } == null
            }.forEach { unlauncherApp ->
                unlauncherAppsBuilder.removeApps(
                    unlauncherAppsBuilder.appsList.indexOf(
                        unlauncherApp
                    )
                )
            }

            if (appAdded) {
                sortAppsAlphabetically(unlauncherAppsBuilder)
            }
            unlauncherAppsBuilder.build()
        }
    }

    suspend fun setHomeApps(apps: List<HomeApp>) {
        unlauncherAppsStore.updateData { unlauncherApps ->
            val unlauncherAppsBuilder = unlauncherApps.toBuilder()
            val unlauncherHomeApps = mutableListOf<UnlauncherApp>()

            // Set home apps
            apps.forEach { homeApp ->
                findApp(
                    unlauncherAppsBuilder.appsList,
                    homeApp.packageName,
                    homeApp.activityName
                )?.let { unlauncherApp ->
                    if (!unlauncherApp.homeApp) {
                        val index = unlauncherAppsBuilder.appsList.indexOf(unlauncherApp)
                        if (index >= 0) {
                            unlauncherAppsBuilder.setApps(
                                index,
                                unlauncherApp.toBuilder().setHomeApp(true)
                                    .setDisplayInDrawer(false)
                                    .build()
                            )
                        }
                    }
                    unlauncherHomeApps.add(unlauncherApp)
                }
            }

            // Clear out old home apps
            unlauncherAppsBuilder.appsList
                .filter { findApp(unlauncherHomeApps, it.packageName, it.className) == null }
                .filter { it.homeApp }
                .forEach { unlauncherApp ->
                    val index = unlauncherAppsBuilder.appsList.indexOf(unlauncherApp)
                    if (index >= 0) {
                        unlauncherAppsBuilder.setApps(
                            index,
                            unlauncherApp.toBuilder().setHomeApp(false).setDisplayInDrawer(true)
                                .build()
                        )
                    }
                }

            unlauncherAppsBuilder.build()
        }
    }

    fun updateDisplayInDrawer(appToUpdate: UnlauncherApp, displayInDrawer: Boolean) {
        lifecycleScope.launch {
            unlauncherAppsStore.updateData { currentApps ->
                val builder = currentApps.toBuilder()
                val index = builder.appsList.indexOf(appToUpdate)
                if (index >= 0) {
                    builder.setApps(
                        index,
                        appToUpdate.toBuilder().setDisplayInDrawer(displayInDrawer)
                    )
                }
                builder.build()
            }
        }
    }

    private fun findApp(
        unlauncherApps: List<UnlauncherApp>,
        packageName: String,
        className: String
    ): UnlauncherApp? {
        return unlauncherApps.firstOrNull { app ->
            packageName == app.packageName && className == app.className
        }
    }
}

fun sortAppsAlphabetically(unlauncherAppsBuilder: UnlauncherApps.Builder) {
    val sortedApps =
        unlauncherAppsBuilder.appsList.sortedBy { it.displayName.toUpperCase(Locale.getDefault()) }
    unlauncherAppsBuilder.clearApps()
    unlauncherAppsBuilder.addAllApps(sortedApps)
}
