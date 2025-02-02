package com.jkuester.unlauncher.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.jkuester.unlauncher.datasource.quickbuttonprefs.QuickButtonPreferencesSerializer
import com.jkuester.unlauncher.datasource.quickbuttonprefs.ToThreeQuickButtonsMigration
import com.jkuester.unlauncher.datasource.quickbuttonprefs.sharedPrefsMigration as quickButtonSharedPrefsMigration
import com.jkuester.unlauncher.datastore.CorePreferences
import com.jkuester.unlauncher.datastore.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.UnlauncherApps
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsMigrations
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsSerializer
import com.sduduzog.slimlauncher.datasource.coreprefs.CorePreferencesMigrations
import com.sduduzog.slimlauncher.datasource.coreprefs.CorePreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.quickButtonPreferencesStore: DataStore<QuickButtonPreferences> by dataStore(
    fileName = "quick_button_preferences.proto",
    serializer = QuickButtonPreferencesSerializer,
    produceMigrations = { context ->
        listOf(
            quickButtonSharedPrefsMigration(context),
            ToThreeQuickButtonsMigration
        )
    }
)

private val Context.unlauncherAppsStore: DataStore<UnlauncherApps> by dataStore(
    fileName = "unlauncher_apps.proto",
    serializer = UnlauncherAppsSerializer,
    produceMigrations = { context -> UnlauncherAppsMigrations().get(context) }
)

private val Context.corePreferencesStore: DataStore<CorePreferences> by dataStore(
    fileName = "core_preferences.proto",
    serializer = CorePreferencesSerializer,
    produceMigrations = { _ -> CorePreferencesMigrations().get() }
)

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Singleton
    @Provides
    fun provideQuickButtonPreferencesStore(
        @ApplicationContext appContext: Context
    ): DataStore<QuickButtonPreferences> = appContext.quickButtonPreferencesStore

    @Singleton
    @Provides
    fun provideUnlauncherAppsStore(
        @ApplicationContext appContext: Context
    ): DataStore<UnlauncherApps> = appContext.unlauncherAppsStore

    @Singleton
    @Provides
    fun provideCorePreferencesStore(
        @ApplicationContext appContext: Context
    ): DataStore<CorePreferences> = appContext.corePreferencesStore
}
