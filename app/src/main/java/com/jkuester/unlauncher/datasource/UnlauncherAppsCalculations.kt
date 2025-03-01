package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.HomeApp
import java.util.Locale

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

private fun setHomeApp(homeAppIndex: Int?): (UnlauncherApp) -> UnlauncherApp = { app ->
    app
        .toBuilder()
        .let {
            when (homeAppIndex) {
                null -> it.clearHomeAppIndex().setDisplayInDrawer(true)
                else -> it.setHomeAppIndex(homeAppIndex).setDisplayInDrawer(false)
            }
        }
        .build()
}

private fun setHomeApp(homeAppIndex: Int, app: UnlauncherApp): UnlauncherApp = setHomeApp(homeAppIndex)(app)

fun setHomeApps(newHomeApps: List<UnlauncherApp>): (UnlauncherApps) -> UnlauncherApps = curried@{ originalApps ->
    val originalHomeApps = originalApps.appsList
        .filter { it.hasHomeAppIndex() }
        .sortedBy { it.homeAppIndex }

    if (originalHomeApps == newHomeApps) {
        return@curried originalApps
    }

    val modifiedApps = originalHomeApps
        .minus(newHomeApps.toSet())
        .map(setHomeApp(null))
        .plus(newHomeApps.mapIndexed(::setHomeApp))
    originalApps.appsList
        .filter(unlauncherAppNotFound(modifiedApps))
        .plus(modifiedApps)
        .sortedBy(::unlauncherAppOrder)
        .let { buildUnlauncherApps(originalApps, it) }
}

fun importHomeApps(apps: List<HomeApp>): (UnlauncherApps) -> UnlauncherApps = { originalApps ->
    val newHomeApps = apps
        .mapNotNull(findUnlauncherApp(originalApps.appsList))
    setHomeApps(newHomeApps)(originalApps)
}

fun addHomeApp(appToUpdate: UnlauncherApp): (UnlauncherApps) -> UnlauncherApps = { originalApps ->
    val newHomeAppIndex = originalApps.appsList
        .filter { it.hasHomeAppIndex() }
        .size
    updateApp(appToUpdate, setHomeApp(newHomeAppIndex))(originalApps)
}

fun getHomeApps(unlauncherApps: UnlauncherApps): List<UnlauncherApp> = unlauncherApps.appsList
    .filter { it.hasHomeAppIndex() }
    .sortedBy { it.homeAppIndex }

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
