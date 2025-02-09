package com.jkuester.unlauncher.datasource

import androidx.datastore.core.DataMigration
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences

object AddClockTypeMigration : DataMigration<CorePreferences> {
    override suspend fun shouldMigrate(currentData: CorePreferences) = !currentData.hasClockType()
    override suspend fun migrate(currentData: CorePreferences): CorePreferences =
        setClockType(ClockType.digital)(currentData)
    override suspend fun cleanUp() {}
}

object AddShowSearchBarMigration : DataMigration<CorePreferences> {
    override suspend fun shouldMigrate(currentData: CorePreferences) = !currentData.hasShowSearchBar()
    override suspend fun migrate(currentData: CorePreferences): CorePreferences = setShowSearchBar(true)(currentData)
    override suspend fun cleanUp() {}
}
