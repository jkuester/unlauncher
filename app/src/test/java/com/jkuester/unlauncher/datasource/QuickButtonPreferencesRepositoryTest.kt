package com.jkuester.unlauncher.datasource

import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class QuickButtonPreferencesRepositoryTest {
    @Test
    fun setLeftIconId() {
        val prefs = QuickButtonPreferences.newBuilder().build()

        val updatedPrefs =
            setLeftIconId(QuickButtonIcon.IC_COG.prefId)(prefs)

        assertEquals(QuickButtonIcon.IC_COG.prefId, updatedPrefs.leftButton.iconId)
        assertEquals(0, updatedPrefs.rightButton.iconId)
        assertEquals(0, updatedPrefs.centerButton.iconId)
    }

    @Test
    fun setCenterIconId() {
        val prefs = QuickButtonPreferences.newBuilder().build()

        val updatedPrefs =
            setCenterIconId(QuickButtonIcon.IC_COG.prefId)(prefs)

        assertEquals(QuickButtonIcon.IC_COG.prefId, updatedPrefs.centerButton.iconId)
        assertEquals(0, updatedPrefs.rightButton.iconId)
        assertEquals(0, updatedPrefs.leftButton.iconId)
    }

    @Test
    fun setRightIconId() {
        val prefs = QuickButtonPreferences.newBuilder().build()

        val updatedPrefs =
            setRightIconId(QuickButtonIcon.IC_COG.prefId)(prefs)

        assertEquals(QuickButtonIcon.IC_COG.prefId, updatedPrefs.rightButton.iconId)
        assertEquals(0, updatedPrefs.leftButton.iconId)
        assertEquals(0, updatedPrefs.centerButton.iconId)
    }

    @ParameterizedTest
    @EnumSource(QuickButtonIcon::class)
    fun getIconResourceId_found(icon: QuickButtonIcon) {
        val result = getIconResourceId(icon.prefId)
        assertEquals(icon.resourceId, result)
    }

    @Test
    fun getIconResourceId_notFound() {
        val result = getIconResourceId(12345)
        assertNull(result)
    }

    @Test
    fun constructQuickButtonPreferencesRepository() = runTest {
        val dataStore = mockk<DataStore<QuickButtonPreferences>>()
        every { dataStore.data } returns emptyFlow()
        assertDoesNotThrow { QuickButtonPreferencesRepository(dataStore, backgroundScope) }
        verify(exactly = 1) { dataStore.data }
    }
}
