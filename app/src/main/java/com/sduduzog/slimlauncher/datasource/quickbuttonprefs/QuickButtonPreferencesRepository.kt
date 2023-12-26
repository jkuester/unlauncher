package com.sduduzog.slimlauncher.datasource.quickbuttonprefs

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.jkuester.unlauncher.datastore.QuickButtonPreferences
import com.sduduzog.slimlauncher.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class QuickButtonPreferencesRepository(
    private val quickButtonPreferencesStore: DataStore<QuickButtonPreferences>,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    companion object {
        const val IC_EMPTY = 1
        const val IC_CALL = 2
        const val IC_COG = 3
        const val IC_PHOTO_CAMERA = 4
        val RES_BY_ICON = mapOf(
            IC_CALL to R.drawable.ic_call,
            IC_COG to R.drawable.ic_cog,
            IC_PHOTO_CAMERA to R.drawable.ic_photo_camera,
            IC_EMPTY to R.drawable.ic_empty
        )
    }

    private val quickButtonPreferencesFlow: Flow<QuickButtonPreferences> =
        quickButtonPreferencesStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(
                        "QuickButtonPrefRepo",
                        "Error reading quick button preferences.",
                        exception
                    )
                    emit(QuickButtonPreferences.getDefaultInstance())
                } else {
                    throw exception
                }
            }

    fun liveData(): LiveData<QuickButtonPreferences> {
        return quickButtonPreferencesFlow.asLiveData()
    }

    fun get(): QuickButtonPreferences {
        return runBlocking {
            quickButtonPreferencesFlow.first()
        }
    }

    fun updateLeftIconId(iconId: Int) {
        lifecycleScope.launch {
            quickButtonPreferencesStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setLeftButton(
                        currentPreferences.leftButton.toBuilder().setIconId(iconId).build()
                    )
                    .build()
            }
        }
    }

    fun updateCenterIconId(iconId: Int) {
        lifecycleScope.launch {
            quickButtonPreferencesStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setCenterButton(
                        currentPreferences.centerButton.toBuilder().setIconId(iconId).build()
                    )
                    .build()
            }
        }
    }

    fun updateRightIconId(iconId: Int) {
        lifecycleScope.launch {
            quickButtonPreferencesStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setRightButton(
                        currentPreferences.rightButton.toBuilder().setIconId(iconId).build()
                    )
                    .build()
            }
        }
    }
}