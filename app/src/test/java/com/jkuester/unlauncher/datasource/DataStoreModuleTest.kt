package com.jkuester.unlauncher.datasource

import android.app.Application
import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class DataStoreModuleTest {
    private val dataStoreModule = DataStoreModule()

    @MockK
    lateinit var appContext: Application

    @BeforeEach
    fun beforeEach() {
        every { appContext.applicationContext } returns appContext
    }

    @AfterEach
    fun afterEach() = verify { appContext.applicationContext }

    @Test
    fun provideQuickButtonPreferencesStore() {
        val actualStore = dataStoreModule.provideQuickButtonPreferencesStore(appContext)
        actualStore.shouldBeInstanceOf<DataStore<QuickButtonPreferences>>()
    }

    @Test
    fun provideUnlauncherAppsStore() {
        val actualStore = dataStoreModule.provideUnlauncherAppsStore(appContext)
        actualStore.shouldBeInstanceOf<DataStore<QuickButtonPreferences>>()
    }

    @Test
    fun provideCorePreferencesStore() {
        val actualStore = dataStoreModule.provideCorePreferencesStore(appContext)
        actualStore.shouldBeInstanceOf<DataStore<QuickButtonPreferences>>()
    }
}
