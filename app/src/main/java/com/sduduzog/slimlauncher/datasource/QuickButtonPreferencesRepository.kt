package com.sduduzog.slimlauncher.datasource

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.jkuester.unlauncher.datastore.QuickButtonPreferences
import com.sduduzog.slimlauncher.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import java.io.IOException

class QuickButtonPreferencesRepository(private val quickButtonPreferencesStore: DataStore<QuickButtonPreferences>) {
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

    suspend fun updateLeftIconId(iconId: Int) {
        quickButtonPreferencesStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setLeftIconId(iconId).build()
        }
    }

    suspend fun updateCenterIconId(iconId: Int) {
        quickButtonPreferencesStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setCenterIconId(iconId).build()
        }
    }

    suspend fun updateRightIconId(iconId: Int) {
        quickButtonPreferencesStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setRightIconId(iconId).build()
        }
    }

    private fun validateQuickButtonPreferences(prefs: QuickButtonPreferences): QuickButtonPreferences {
        if (prefs.leftIconId == 0 || prefs.centerIconId == 0 || prefs.rightIconId == 0) {
            val prefBuilder = prefs.toBuilder()
            if (prefs.leftIconId == 0) {
                prefBuilder.leftIconId = DEFAULT_ICON_LEFT
            }
            if (prefs.centerIconId == 0) {
                prefBuilder.centerIconId = DEFAULT_ICON_CENTER
            }
            if (prefs.rightIconId == 0) {
                prefBuilder.rightIconId = DEFAULT_ICON_RIGHT
            }
            return prefBuilder.build()
        }
        return prefs
    }
}