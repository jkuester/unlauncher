package com.sduduzog.slimlauncher.datasource.coreprefs

import androidx.datastore.core.DataMigration
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences

class CorePreferencesMigrations {
    fun get(): List<DataMigration<CorePreferences>> = listOf(
        object : DataMigration<CorePreferences> {
            override suspend fun shouldMigrate(currentData: CorePreferences) = !currentData.hasClockType()
            override suspend fun migrate(currentData: CorePreferences) =
                currentData.toBuilder().setClockType(ClockType.digital).build()
            override suspend fun cleanUp() {}
        },
        object : DataMigration<CorePreferences> {
            override suspend fun shouldMigrate(currentData: CorePreferences) = !currentData.hasShowSearchBar()
            override suspend fun migrate(currentData: CorePreferences) =
                currentData.toBuilder().setShowSearchBar(true).build()
            override suspend fun cleanUp() {}
        }
    )
}
