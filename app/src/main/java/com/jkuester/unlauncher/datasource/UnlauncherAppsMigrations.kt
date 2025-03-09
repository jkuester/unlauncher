package com.jkuester.unlauncher.datasource

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataMigration
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.di.AppModule

object SortAppsMigration : DataMigration<UnlauncherApps> {
    private const val VERSION = 1

    override suspend fun shouldMigrate(currentData: UnlauncherApps) = currentData.version < VERSION
    override suspend fun migrate(currentData: UnlauncherApps) = sortApps(currentData)
        .let(setVersion(VERSION))
    override suspend fun cleanUp() {}
}

class HomeAppToIndexMigration(context: Context) : DataMigration<UnlauncherApps> {
    private val homeAppDao = AppModule()
        .provideBaseDatabase(context as Application)
        .baseDao()

    override suspend fun shouldMigrate(currentData: UnlauncherApps) = homeAppDao.getAll().isNotEmpty()
    override suspend fun migrate(currentData: UnlauncherApps): UnlauncherApps =
        importHomeApps(homeAppDao.getAll())(currentData)
    override suspend fun cleanUp() = homeAppDao.clearTable()
}
