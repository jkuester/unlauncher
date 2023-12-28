package com.sduduzog.slimlauncher.datasource.coreprefs

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.jkuester.unlauncher.datastore.CorePreferences
import com.jkuester.unlauncher.datastore.SearchBarPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class CorePreferencesRepository(
    private val corePreferencesStore: DataStore<CorePreferences>,
    private val lifecycleScope: LifecycleCoroutineScope
) {
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

    fun liveData(): LiveData<CorePreferences> {
        return corereferencesFlow.asLiveData()
    }

    fun get(): CorePreferences {
        return runBlocking {
            corereferencesFlow.first()
        }
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

    private fun updateShowSearchBar(showSearchBar: Boolean) {
        lifecycleScope.launch {
            corePreferencesStore.updateData {
                it.toBuilder().setShowSearchBar(showSearchBar).build()
            }
        }
    }

    var showSearchField: Boolean
        // when upgrading from an older version the property showSearchBar is null
        // we therefore set default state to true.
        // This has the reason that protobuf 3 does not allow default values,
        // see https://stackoverflow.com/a/62435235,
        // hence making the showSearchBar attribute optional and allow it to be null.
        // With that we can use a logical implication: hasShowSearchBar -> showSearchBar,
        // returning true, when the showSearchBar attribute is null.
        get() = !get().hasShowSearchBar() || get().showSearchBar
        set(value) = updateShowSearchBar(value)

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
}