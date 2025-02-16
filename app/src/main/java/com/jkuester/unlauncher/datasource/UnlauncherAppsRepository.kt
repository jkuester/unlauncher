package com.jkuester.unlauncher.datasource

import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.fragment.LifecycleOwnerSupplier
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.HomeApp
import dagger.hilt.android.scopes.FragmentScoped
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

private fun appMatches(unlauncherApp: UnlauncherApp, packageName: String, className: String) =
    unlauncherApp.packageName == packageName && unlauncherApp.className == className
private fun appMatches(app: App): (UnlauncherApp) -> Boolean = {
    appMatches(it, app.packageName, app.activityName)
}
private fun appMatches(unlauncherApp: UnlauncherApp): (App) -> Boolean = {
    appMatches(unlauncherApp, it.packageName, it.activityName)
}
private fun appMatches(homeApp: HomeApp): (UnlauncherApp) -> Boolean = {
    appMatches(it, homeApp.packageName, homeApp.activityName)
}
private fun unlauncherAppMatches(app: UnlauncherApp): (UnlauncherApp) -> Boolean = {
    appMatches(it, app.packageName, app.className)
}
private fun findUnlauncherApp(unlauncherApps: List<UnlauncherApp>): (HomeApp) -> UnlauncherApp? = { homeApp ->
    unlauncherApps.firstOrNull(appMatches(homeApp))
}
private fun unlauncherAppNotFound(unlauncherApps: List<UnlauncherApp>): (UnlauncherApp) -> Boolean = { app ->
    unlauncherApps.firstOrNull(unlauncherAppMatches(app)) == null
}
private fun unlauncherAppNotFound(unlauncherApps: UnlauncherApps): (App) -> Boolean = { app ->
    unlauncherApps.appsList.firstOrNull(appMatches(app)) == null
}
private fun appNotFound(apps: List<App>): (UnlauncherApp) -> Boolean = { unlauncherApp ->
    apps.firstOrNull(appMatches(unlauncherApp)) == null
}

private fun buildUnlauncherApp(app: App): UnlauncherApp = UnlauncherApp
    .newBuilder()
    .setPackageName(app.packageName)
    .setClassName(app.activityName)
    .setUserSerial(app.userSerial)
    .setDisplayName(app.appName)
    .setDisplayInDrawer(true)
    .build()

private fun buildUnlauncherApps(unlauncherApps: UnlauncherApps, apps: List<UnlauncherApp>): UnlauncherApps =
    unlauncherApps
        .toBuilder()
        .clearApps()
        .addAllApps(apps)
        .build()

private fun unlauncherAppOrder(app: UnlauncherApp) = app.displayName.uppercase(Locale.getDefault())

fun setApps(apps: List<App>): (UnlauncherApps) -> UnlauncherApps = { originalApps ->
    val appsToAdd = apps
        .filter(unlauncherAppNotFound(originalApps))
        .map(::buildUnlauncherApp)
    val appsToRemove = originalApps.appsList
        .filter(appNotFound(apps))
    if (appsToAdd.isEmpty() && appsToRemove.isEmpty()) {
        originalApps
    } else {
        originalApps.appsList
            .filter { app -> !appsToRemove.contains(app) }
            .plus(appsToAdd)
            .sortedBy(::unlauncherAppOrder)
            .let { buildUnlauncherApps(originalApps, it) }
    }
}

private fun setHomeApp(isHomeApp: Boolean): (UnlauncherApp) -> UnlauncherApp = {
    it.toBuilder()
        .setHomeApp(isHomeApp)
        .setDisplayInDrawer(!isHomeApp)
        .build()
}

fun setHomeApps(apps: List<HomeApp>): (UnlauncherApps) -> UnlauncherApps = { originalApps ->
    val originalHomeApps = originalApps.appsList
        .filter { it.homeApp }
        .toSet()
    val newHomeApps = apps
        .mapNotNull(findUnlauncherApp(originalApps.appsList))
        .toSet()
    val appsToRemove = originalHomeApps
        .minus(newHomeApps)
        .map(setHomeApp(false))
    val appsToAdd = newHomeApps
        .minus(originalHomeApps)
        .map(setHomeApp(true))
    val modifiedApps = appsToRemove.plus(appsToAdd)
    if (modifiedApps.isEmpty()) {
        originalApps
    } else {
        originalApps.appsList
            .filter(unlauncherAppNotFound(modifiedApps))
            .plus(modifiedApps)
            .sortedBy(::unlauncherAppOrder)
            .let { buildUnlauncherApps(originalApps, it) }
    }
}

fun sortApps(unlauncherApps: UnlauncherApps): UnlauncherApps = unlauncherApps
    .toBuilder()
    .clearApps()
    .addAllApps(unlauncherApps.appsList.sortedBy(::unlauncherAppOrder))
    .build()

private fun updateApp(
    appToUpdate: UnlauncherApp,
    update: (UnlauncherApp) -> UnlauncherApp
): (UnlauncherApps) -> UnlauncherApps = { originalApps ->
    when (val i = originalApps.appsList.indexOf(appToUpdate)) {
        -1 -> originalApps
        else -> originalApps
            .toBuilder()
            .setApps(i, update(appToUpdate))
            .build()
    }
}

fun setDisplayName(appToUpdate: UnlauncherApp, displayName: String): (UnlauncherApps) -> UnlauncherApps =
    { originalApps ->
        updateApp(appToUpdate) { it.toBuilder().setDisplayName(displayName).build() }(originalApps)
            .let(::sortApps)
    }

fun setDisplayInDrawer(appToUpdate: UnlauncherApp, displayInDrawer: Boolean): (UnlauncherApps) -> UnlauncherApps =
    updateApp(appToUpdate) { it.toBuilder().setDisplayInDrawer(displayInDrawer).build() }

fun setVersion(version: Int): (UnlauncherApps) -> UnlauncherApps = { it.toBuilder().setVersion(version).build() }

@FragmentScoped
class UnlauncherAppsRepository @Inject constructor(
    unlauncherAppsStore: DataStore<UnlauncherApps>,
    lifecycleScope: CoroutineScope,
    lifecycleOwnerSupplier: LifecycleOwnerSupplier
) : AbstractDataRepository<UnlauncherApps>(
    unlauncherAppsStore,
    lifecycleScope,
    lifecycleOwnerSupplier,
    UnlauncherApps::getDefaultInstance
)

object UnlauncherAppsSerializer : AbstractDataSerializer<UnlauncherApps>(
    UnlauncherApps::getDefaultInstance,
    UnlauncherApps::parseFrom
)
