package com.sduduzog.slimlauncher.datasource.coreprefs

import androidx.datastore.core.DataMigration
import com.jkuester.unlauncher.datastore.ClockType
import com.jkuester.unlauncher.datastore.CorePreferences

class CorePreferencesMigrations {
    fun get(): List<DataMigration<CorePreferences>> {
        return listOf(
            object : DataMigration<CorePreferences> {
                override suspend fun shouldMigrate(currentData: CorePreferences) = !currentData.hasClockType()

                override suspend fun migrate(currentData: CorePreferences): CorePreferences {
                    val prefBuilder = currentData.toBuilder()
                    prefBuilder.clockType = ClockType.digital
                    return prefBuilder.build()
                }

                override suspend fun cleanUp() {}
            }
        )
    }
}