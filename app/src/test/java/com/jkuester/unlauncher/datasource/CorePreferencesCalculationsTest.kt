package com.jkuester.unlauncher.datasource
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.FontSize
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import com.jkuester.unlauncher.datastore.proto.Theme
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.sduduzog.slimlauncher.R
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

private val EMPTY_PREFS = CorePreferences.newBuilder().build()

@MockKExtension.CheckUnnecessaryStub
@MockKExtension.ConfirmVerification
@ExtendWith(MockKExtension::class)
class CorePreferencesCalculationsTest {
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
    fun testToggleShowDrawerHeadings() {
        val updatedPrefs = toggleShowDrawerHeadings()(EMPTY_PREFS)
        updatedPrefs.showDrawerHeadings shouldBe true

        val updatedPrefs1 = toggleShowDrawerHeadings()(updatedPrefs)
        updatedPrefs1.showDrawerHeadings shouldBe false
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

    @ParameterizedTest
    @EnumSource(
        value = TimeFormat::class,
        names = ["UNRECOGNIZED"],
        mode = EnumSource.Mode.EXCLUDE
    )
    fun testSetTimeFormat(timeFormat: TimeFormat) {
        val updatedPrefs = setTimeFormat(timeFormat)(EMPTY_PREFS)
        updatedPrefs.timeFormat shouldBe timeFormat
    }

    @ParameterizedTest
    @EnumSource(
        value = Theme::class,
        names = ["UNRECOGNIZED"],
        mode = EnumSource.Mode.EXCLUDE
    )
    fun testSetTheme(theme: Theme) {
        val updatedPrefs = setTheme(theme)(EMPTY_PREFS)
        updatedPrefs.theme shouldBe theme
    }

    @ParameterizedTest
    @EnumSource(Theme::class)
    fun testGetThemeStyleResource(theme: Theme) {
        val expectedStyles = arrayOf(
            R.style.AppTheme,
            R.style.AppThemeDark,
            R.style.AppGreyTheme,
            R.style.AppTealTheme,
            R.style.AppCandyTheme,
            R.style.AppPinkTheme,
            R.style.AppThemeLight,
            R.style.AppDarculaTheme,
            R.style.AppGruvBoxDarkTheme,
            R.style.AppTheme,
        )

        val resId = getThemeStyleResource(theme)

        resId shouldBe expectedStyles[theme.ordinal]
    }

    @Test
    fun testSetHideStatusBar() {
        val updatedPrefs = setHideStatusBar(true)(EMPTY_PREFS)
        updatedPrefs.hideStatusBar shouldBe true
    }

    @Test
    fun testToggleHideStatusBar() {
        val updatedPrefs = toggleHideStatusBar()(EMPTY_PREFS)
        updatedPrefs.hideStatusBar shouldBe true

        val updatedPrefs1 = toggleHideStatusBar()(updatedPrefs)
        updatedPrefs1.hideStatusBar shouldBe false
    }

    @ParameterizedTest
    @EnumSource(
        value = FontSize::class,
        names = ["UNRECOGNIZED"],
        mode = EnumSource.Mode.EXCLUDE
    )
    fun testSetFontSize(fontSize: FontSize) {
        val updatedPrefs = setFontSize(fontSize)(EMPTY_PREFS)
        updatedPrefs.fontSize shouldBe fontSize
    }

    @ParameterizedTest
    @EnumSource(FontSize::class)
    fun testGetScaledAppSize(fontSize: FontSize) {
        val expectedSizes = arrayOf(19.2f, 24f, 28.8f, 24f)
        val size = getScaledAppSize(fontSize)
        size shouldBe (expectedSizes[fontSize.ordinal] plusOrMinus 0.01f)
    }

    @ParameterizedTest
    @EnumSource(FontSize::class)
    fun testGetScaledClockSize(fontSize: FontSize) {
        val expectedSizes = arrayOf(32f, 40f, 48f, 40f)
        val size = getScaledClockSize(fontSize)
        size shouldBe (expectedSizes[fontSize.ordinal] plusOrMinus 0.01f)
    }

    @ParameterizedTest
    @EnumSource(FontSize::class)
    fun testGetScaledDateSize(fontSize: FontSize) {
        val expectedSizes = arrayOf(14.4f, 18f, 21.6f, 18f)
        val size = getScaledDateSize(fontSize)
        size shouldBe (expectedSizes[fontSize.ordinal] plusOrMinus 0.01f)
    }
}
