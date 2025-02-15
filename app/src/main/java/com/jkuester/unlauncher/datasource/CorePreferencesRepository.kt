package com.jkuester.unlauncher.datasource

import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

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
fun setShowDrawerHeadings(showDrawerHeadings: Boolean) = { originalPrefs: CorePreferences ->
    originalPrefs.toBuilder().setShowDrawerHeadings(showDrawerHeadings).build()
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

@ActivityScoped
class CorePreferencesRepository @Inject constructor(
    corePreferencesStore: DataStore<CorePreferences>,
    lifecycleScope: CoroutineScope
) : AbstractDataRepository<CorePreferences>(
    corePreferencesStore,
    lifecycleScope,
    CorePreferences::getDefaultInstance
)

object CorePreferencesSerializer : AbstractDataSerializer<CorePreferences>(
    CorePreferences::getDefaultInstance,
    CorePreferences::parseFrom
)
