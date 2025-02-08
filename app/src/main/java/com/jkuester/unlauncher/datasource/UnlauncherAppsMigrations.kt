@file:Suppress("ktlint:standard:filename")

package com.jkuester.unlauncher.datasource

import androidx.datastore.core.DataMigration
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps

object SortAppsMigration : DataMigration<UnlauncherApps> {
    private const val VERSION = 1

    override suspend fun shouldMigrate(currentData: UnlauncherApps) = currentData.version < VERSION
    override suspend fun migrate(currentData: UnlauncherApps) = sortApps(currentData)
        .let(setVersion(VERSION))
    override suspend fun cleanUp() {}
}
