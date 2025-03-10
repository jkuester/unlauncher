package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import com.jkuester.unlauncher.datastore.proto.TimeFormat

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
