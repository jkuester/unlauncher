package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private val EMPTY_PREFS = CorePreferences.newBuilder().build()

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CorePreferencesRepositoryTest {
    @Test
    fun testToggleActivateKeyboardInDrawer() {
        val updatedPrefs = toggleActivateKeyboardInDrawer()(EMPTY_PREFS)
        updatedPrefs.activateKeyboardInDrawer shouldBe true

        val updatedPrefs1 = toggleActivateKeyboardInDrawer()(updatedPrefs)
        updatedPrefs1.activateKeyboardInDrawer shouldBe false
    }

    @Test
    fun setKeepDeviceWallpaper() {
        val updatedPrefs = setKeepDeviceWallpaper(true)(EMPTY_PREFS)
        updatedPrefs.keepDeviceWallpaper shouldBe true
    }

    @Test
    fun setShowSearchBar() {
        val updatedPrefs = setShowSearchBar(true)(EMPTY_PREFS)
        updatedPrefs.showSearchBar shouldBe true
    }

    @Test
    fun setSearchBarPosition() {
        val updatedPrefs = setSearchBarPosition(SearchBarPosition.top)(EMPTY_PREFS)
        updatedPrefs.searchBarPosition shouldBe SearchBarPosition.top
    }

    @Test
    fun setShowDrawerHeadings() {
        val updatedPrefs = setShowDrawerHeadings(true)(EMPTY_PREFS)
        updatedPrefs.showDrawerHeadings shouldBe true
    }

    @Test
    fun testToggleSearchAllAppsInDrawer() {
        val updatedPrefs = toggleSearchAllAppsInDrawer()(EMPTY_PREFS)
        updatedPrefs.searchAllAppsInDrawer shouldBe true

        val updatedPrefs1 = toggleSearchAllAppsInDrawer()(updatedPrefs)
        updatedPrefs1.searchAllAppsInDrawer shouldBe false
    }

    @Test
    fun setClockType() {
        val updatedPrefs = setClockType(ClockType.digital)(EMPTY_PREFS)
        updatedPrefs.clockType shouldBe ClockType.digital
    }

    @Test
    fun setAlignmentFormat() {
        val updatedPrefs = setAlignmentFormat(AlignmentFormat.center)(EMPTY_PREFS)
        updatedPrefs.alignmentFormat shouldBe AlignmentFormat.center
    }
}
