package com.sduduzog.slimlauncher.datasource.apps

import android.content.Context
import androidx.datastore.core.DataMigration
import com.jkuester.unlauncher.datastore.UnlauncherApps

class UnlauncherAppsMigrations {

    fun get(context: Context): List<DataMigration<UnlauncherApps>> {
        return listOf(object : UnlauncherAppsMigration(1) {
            // Re-sort the apps with new alphabetizing scheme
            override suspend fun migrate(currentData: UnlauncherApps): UnlauncherApps {
                val builder = currentData.toBuilder()
                sortAppsAlphabetically(builder)
                return updateVersion(builder)
            }
        })
    }

    abstract class UnlauncherAppsMigration(private val version: Int) :
        DataMigration<UnlauncherApps> {
        override suspend fun shouldMigrate(currentData: UnlauncherApps): Boolean {
            return currentData.version < version
        }

        override suspend fun cleanUp() {}

        fun updateVersion(builder: UnlauncherApps.Builder): UnlauncherApps {
            return builder.setVersion(version).build()
        }
    }
}
