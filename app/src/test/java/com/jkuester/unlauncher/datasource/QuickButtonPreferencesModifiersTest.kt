package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class QuickButtonPreferencesModifiersTest {
    @Test
    fun setLeftIconId() {
        val prefs = QuickButtonPreferences.newBuilder().build()

        val updatedPrefs =
            setLeftIconId(QuickButtonIcon.IC_COG.prefId)(prefs)

        updatedPrefs.leftButton.iconId shouldBe QuickButtonIcon.IC_COG.prefId
        updatedPrefs.rightButton.iconId shouldBe 0
        updatedPrefs.centerButton.iconId shouldBe 0
    }

    @Test
    fun setCenterIconId() {
        val prefs = QuickButtonPreferences.newBuilder().build()

        val updatedPrefs =
            setCenterIconId(QuickButtonIcon.IC_COG.prefId)(prefs)

        updatedPrefs.centerButton.iconId shouldBe QuickButtonIcon.IC_COG.prefId
        updatedPrefs.rightButton.iconId shouldBe 0
        updatedPrefs.leftButton.iconId shouldBe 0
    }

    @Test
    fun setRightIconId() {
        val prefs = QuickButtonPreferences.newBuilder().build()

        val updatedPrefs =
            setRightIconId(QuickButtonIcon.IC_COG.prefId)(prefs)

        updatedPrefs.rightButton.iconId shouldBe QuickButtonIcon.IC_COG.prefId
        updatedPrefs.leftButton.iconId shouldBe 0
        updatedPrefs.centerButton.iconId shouldBe 0
    }

    @ParameterizedTest
    @EnumSource(QuickButtonIcon::class)
    fun getIconResourceId_found(icon: QuickButtonIcon) {
        val result = getIconResourceId(icon.prefId)
        result shouldBe icon.resourceId
    }

    @Test
    fun getIconResourceId_notFound() {
        val result = getIconResourceId(12345)
        result shouldBe null
    }
}
