package com.sduduzog.slimlauncher.datasource.coreprefs

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.SearchBarPosition
import dagger.hilt.android.scopes.ActivityScoped
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@ActivityScoped
class CorePreferencesRepository @Inject constructor(
    private val corePreferencesStore: DataStore<CorePreferences>,
    activity: Activity
) {
    private val lifecycleScope = (activity as ComponentActivity).lifecycleScope
    private val corereferencesFlow: Flow<CorePreferences> =
        corePreferencesStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(
                        "CorePrefRepo",
                        "Error reading core preferences.",
                        exception
                    )
                    emit(CorePreferences.getDefaultInstance())
                } else {
                    throw exception
                }
            }

    fun liveData(): LiveData<CorePreferences> = corereferencesFlow.asLiveData()

    fun get(): CorePreferences = runBlocking {
        corereferencesFlow.first()
    }

    fun updateActivateKeyboardInDrawer(activateKeyboardInDrawer: Boolean) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setActivateKeyboardInDrawer(activateKeyboardInDrawer).build()
            }
        }
    }

    fun updateKeepDeviceWallpaper(keepDeviceWallpaper: Boolean) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setKeepDeviceWallpaper(keepDeviceWallpaper).build()
            }
        }
    }

    fun updateShowSearchBar(showSearchBar: Boolean) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setShowSearchBar(showSearchBar).build()
            }
        }
    }

    fun updateSearchBarPosition(searchBarPosition: SearchBarPosition) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setSearchBarPosition(searchBarPosition).build()
            }
        }
    }

    fun updateShowDrawerHeadings(showDrawerHeadings: Boolean) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setShowDrawerHeadings(showDrawerHeadings).build()
            }
        }
    }

    fun updateSearchAllAppsInDrawer(searchAllAppsInDrawer: Boolean) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setSearchAllAppsInDrawer(searchAllAppsInDrawer).build()
            }
        }
    }

    fun updateClockType(clockType: ClockType) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setClockType(clockType).build()
            }
        }
    }

    fun updateAlignmentFormat(alignmentFormat: AlignmentFormat) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setAlignmentFormat(alignmentFormat).build()
            }
        }
    }
}
