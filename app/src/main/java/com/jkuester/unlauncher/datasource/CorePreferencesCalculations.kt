package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.FontSize
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import com.jkuester.unlauncher.datastore.proto.Theme
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.sduduzog.slimlauncher.R

fun toggleActivateKeyboardInDrawer() = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setActivateKeyboardInDrawer(!originalPrefs.activateKeyboardInDrawer).build()
}
fun setKeepDeviceWallpaper(keepDeviceWallpaper: Boolean) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setKeepDeviceWallpaper(keepDeviceWallpaper).build()
}
fun setShowSearchBar(showSearchBar: Boolean) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setShowSearchBar(showSearchBar).build()
}
fun setSearchBarPosition(searchBarPosition: SearchBarPosition) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setSearchBarPosition(searchBarPosition).build()
}
fun toggleShowDrawerHeadings() = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setShowDrawerHeadings(!originalPrefs.showDrawerHeadings).build()
}
fun toggleSearchAllAppsInDrawer() = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setSearchAllAppsInDrawer(!originalPrefs.searchAllAppsInDrawer).build()
}
fun setClockType(clockType: ClockType) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setClockType(clockType).build()
}
fun setAlignmentFormat(alignmentFormat: AlignmentFormat) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setAlignmentFormat(alignmentFormat).build()
}
fun setTimeFormat(timeFormat: TimeFormat) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setTimeFormat(timeFormat).build()
}
fun setTheme(theme: Theme) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setTheme(theme).build()
}
fun setHideStatusBar(hideStatusBar: Boolean) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setHideStatusBar(hideStatusBar).build()
}
fun toggleHideStatusBar() = { originalPrefs: CorePreferences ->
    setHideStatusBar(!originalPrefs.hideStatusBar)(originalPrefs)
}
fun setFontSize(fontSize: FontSize) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setFontSize(fontSize).build()
}

private val STYLE_RESOURCES_BY_THEME = mapOf(
    Theme.system_theme to R.style.AppTheme,
    Theme.midnight to R.style.AppThemeDark,
    Theme.jupiter to R.style.AppGreyTheme,
    Theme.teal to R.style.AppTealTheme,
    Theme.candy to R.style.AppCandyTheme,
    Theme.pastel to R.style.AppPinkTheme,
    Theme.noon to R.style.AppThemeLight,
    Theme.vlad to R.style.AppDarculaTheme,
    Theme.groovy to R.style.AppGruvBoxDarkTheme,
)

private val FONT_SCALE_FACTORS = mapOf(
    FontSize.small to 0.8f,
    FontSize.medium to 1.0f,
    FontSize.large to 1.2f
)

fun getThemeStyleResource(theme: Theme) = STYLE_RESOURCES_BY_THEME[theme] ?: R.style.AppTheme
fun getFontScaleFactor(fontSize: FontSize) = FONT_SCALE_FACTORS[fontSize] ?: 1.0f
private const val ORIGINAL_CLOCK_SIZE = 40f
private const val ORIGINAL_DATE_SIZE = 18f
private const val ORIGINAL_APP_SIZE = 24f
fun getScaledClockSize(fontSize: FontSize) = ORIGINAL_CLOCK_SIZE * getFontScaleFactor(fontSize)
fun getScaledDateSize(fontSize: FontSize) = ORIGINAL_DATE_SIZE * getFontScaleFactor(fontSize)
fun getScaledAppSize(fontSize: FontSize) = ORIGINAL_APP_SIZE * getFontScaleFactor(fontSize)
