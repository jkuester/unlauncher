package com.jkuester.unlauncher.datasource

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat

private const val SHARED_PREF_GROUP_NAME = "settings"
private const val PREFS_SETTINGS_KEY_TIME_FORMAT = "time_format"

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

fun timeFormatSharedPrefsMigration(context: Context) = SharedPreferencesMigration(
    context,
    SHARED_PREF_GROUP_NAME,
    setOf(PREFS_SETTINGS_KEY_TIME_FORMAT),
    { true },
    { sharedPrefs: SharedPreferencesView, currentData: CorePreferences ->
        val timeFormatPref = sharedPrefs.getInt(PREFS_SETTINGS_KEY_TIME_FORMAT, 0)
        setTimeFormat(TimeFormat.forNumber(timeFormatPref))(currentData)
    }
)
