package com.jkuester.unlauncher.datasource.quickbuttonprefs

import com.jkuester.unlauncher.datastore.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.QuickButtonPreferences.QuickButton
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

private const val ICON_ID = 1

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class QuickButtonPreferencesRepositoryTest {
    @Nested
    inner class IconSettersTest {
        @MockK
        lateinit var prefs: QuickButtonPreferences
        @MockK
        lateinit var updatedPrefs: QuickButtonPreferences
        @MockK
        lateinit var prefBuilder: QuickButtonPreferences.Builder
        @MockK
        lateinit var button: QuickButton
        @MockK
        lateinit var buttonBuilder: QuickButton.Builder

        @BeforeEach
        fun beforeEach() {
            every { prefs.toBuilder() } returns prefBuilder
            every { button.toBuilder() } returns buttonBuilder
            every { buttonBuilder.setIconId(any()) } returns buttonBuilder
            every { prefBuilder.build() } returns updatedPrefs
        }

        @AfterEach
        fun afterEach() {
            verify(exactly = 1) { prefs.toBuilder() }
            verify(exactly = 1) { button.toBuilder() }
            verify(exactly = 1) { buttonBuilder.setIconId(ICON_ID) }
            verify(exactly = 1) { prefBuilder.build() }
        }

        @Test
        fun setLeftIconId() {
            every { prefs.leftButton } returns button
            every { prefBuilder.setLeftButton(any<QuickButton.Builder>()) } returns prefBuilder

            val result = setLeftIconId(ICON_ID)(prefs)

            assertSame(updatedPrefs, result)
            verify(exactly = 1) { prefs.leftButton }
            verify(exactly = 1) { prefBuilder.setLeftButton(buttonBuilder) }
        }

        @Test
        fun setCenterIconId() {
            every { prefs.centerButton } returns button
            every { prefBuilder.setCenterButton(any<QuickButton.Builder>()) } returns prefBuilder

            val result = setCenterIconId(ICON_ID)(prefs)

            assertSame(updatedPrefs, result)
            verify(exactly = 1) { prefs.centerButton }
            verify(exactly = 1) { prefBuilder.setCenterButton(buttonBuilder) }
        }

        @Test
        fun setRightIconId() {
            every { prefs.rightButton } returns button
            every { prefBuilder.setRightButton(any<QuickButton.Builder>()) } returns prefBuilder

            val result = setRightIconId(ICON_ID)(prefs)

            assertSame(updatedPrefs, result)
            verify(exactly = 1) { prefs.rightButton }
            verify(exactly = 1) { prefBuilder.setRightButton(buttonBuilder) }
        }
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
}
