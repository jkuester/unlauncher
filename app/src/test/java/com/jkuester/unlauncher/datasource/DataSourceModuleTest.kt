package com.jkuester.unlauncher.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datastore.CorePreferences
import com.jkuester.unlauncher.datastore.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.UnlauncherApps
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class DataSourceModuleTest {
    private val dataStoreModule = DataStoreModule()

    @MockK
    lateinit var appContext: Context

    @BeforeEach
    fun beforeEach() {
        every { appContext.applicationContext } returns appContext
    }

    @AfterEach
    fun afterEach() = verify { appContext.applicationContext }

    @Test
    fun provideQuickButtonPreferencesStore() {
        val actualStore = dataStoreModule.provideQuickButtonPreferencesStore(appContext)
        assertInstanceOf<DataStore<QuickButtonPreferences>>(actualStore)
    }

    @Test
    fun provideUnlauncherAppsStore() {
        val actualStore = dataStoreModule.provideUnlauncherAppsStore(appContext)
        assertInstanceOf<DataStore<UnlauncherApps>>(actualStore)
    }

    @Test
    fun provideCorePreferencesStore() {
        val actualStore = dataStoreModule.provideCorePreferencesStore(appContext)
        assertInstanceOf<DataStore<CorePreferences>>(actualStore)
    }
}
