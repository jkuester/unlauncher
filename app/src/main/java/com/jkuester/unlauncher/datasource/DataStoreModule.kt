package com.jkuester.unlauncher.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.jkuester.unlauncher.datasource.sharedPrefsMigration as quickButtonSharedPrefsMigration
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.quickButtonPreferencesStore: DataStore<QuickButtonPreferences> by dataStore(
    fileName = "quick_button_preferences.proto",
    serializer = DataSerializer<QuickButtonPreferences>(
        QuickButtonPreferences::getDefaultInstance,
        QuickButtonPreferences::parseFrom
    ),
    produceMigrations = { context ->
        listOf(
            quickButtonSharedPrefsMigration(context),
            ToThreeQuickButtonsMigration
        )
    }
)

private val Context.unlauncherAppsStore: DataStore<UnlauncherApps> by dataStore(
    fileName = "unlauncher_apps.proto",
    serializer = DataSerializer(UnlauncherApps::getDefaultInstance, UnlauncherApps::parseFrom),
    produceMigrations = { context -> listOf(SortAppsMigration, HomeAppToIndexMigration(context)) }
)

private val Context.corePreferencesStore: DataStore<CorePreferences> by dataStore(
    fileName = "core_preferences.proto",
    serializer = DataSerializer(CorePreferences::getDefaultInstance, CorePreferences::parseFrom),
    produceMigrations = { listOf(AddClockTypeMigration, AddShowSearchBarMigration) }
)

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Singleton
    @Provides
    fun provideQuickButtonPreferencesStore(@ApplicationContext appContext: Context): DataStore<QuickButtonPreferences> =
        appContext.quickButtonPreferencesStore

    @Singleton
    @Provides
    fun provideUnlauncherAppsStore(@ApplicationContext appContext: Context): DataStore<UnlauncherApps> =
        appContext.unlauncherAppsStore

    @Singleton
    @Provides
    fun provideCorePreferencesStore(@ApplicationContext appContext: Context): DataStore<CorePreferences> =
        appContext.corePreferencesStore
}
