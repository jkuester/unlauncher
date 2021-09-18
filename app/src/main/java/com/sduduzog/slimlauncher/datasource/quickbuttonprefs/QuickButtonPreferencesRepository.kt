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
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class QuickButtonPreferencesRepository(
    private val quickButtonPreferencesStore: DataStore<QuickButtonPreferences>,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    companion object {
        const val DEFAULT_ICON_LEFT = R.drawable.ic_call
        const val DEFAULT_ICON_CENTER = R.drawable.ic_cog
        const val DEFAULT_ICON_RIGHT = R.drawable.ic_photo_camera
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
            .transform { prefs -> emit(validateQuickButtonPreferences(prefs)) }

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

    private fun validateQuickButtonPreferences(prefs: QuickButtonPreferences): QuickButtonPreferences {
        if (!prefs.hasLeftButton() || !prefs.hasCenterButton() || !prefs.hasRightButton()) {
            val prefBuilder = prefs.toBuilder()
            if (!prefs.hasLeftButton()) {
                prefBuilder.leftButton =
                    QuickButtonPreferences.QuickButton.newBuilder().setIconId(DEFAULT_ICON_LEFT)
                        .build()
            }
            if (!prefs.hasCenterButton()) {
                prefBuilder.centerButton =
                    QuickButtonPreferences.QuickButton.newBuilder().setIconId(DEFAULT_ICON_CENTER)
                        .build()
            }
            if (!prefs.hasRightButton()) {
                prefBuilder.rightButton =
                    QuickButtonPreferences.QuickButton.newBuilder().setIconId(DEFAULT_ICON_RIGHT)
                        .build()
            }
            return prefBuilder.build()
        }
        return prefs
    }
}