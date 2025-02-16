package com.jkuester.unlauncher.datasource

import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences.QuickButton
import com.jkuester.unlauncher.fragment.LifecycleOwnerSupplier
import com.sduduzog.slimlauncher.R
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

private fun setButtonIconId(button: QuickButton, iconId: Int) = button
    .toBuilder()
    .setIconId(iconId)

fun setLeftIconId(iconId: Int) = { currentPreferences: QuickButtonPreferences ->
    currentPreferences
        .toBuilder()
        .setLeftButton(setButtonIconId(currentPreferences.leftButton, iconId))
        .build()
}

fun setCenterIconId(iconId: Int) = { currentPreferences: QuickButtonPreferences ->
    currentPreferences
        .toBuilder()
        .setCenterButton(setButtonIconId(currentPreferences.centerButton, iconId))
        .build()
}

fun setRightIconId(iconId: Int) = { currentPreferences: QuickButtonPreferences ->
    currentPreferences
        .toBuilder()
        .setRightButton(setButtonIconId(currentPreferences.rightButton, iconId))
        .build()
}

enum class QuickButtonIcon(val prefId: Int, val resourceId: Int) {
    IC_EMPTY(1, R.drawable.ic_empty),
    IC_CALL(2, R.drawable.ic_call),
    IC_COG(3, R.drawable.ic_cog),
    IC_PHOTO_CAMERA(4, R.drawable.ic_photo_camera)
}

fun getIconResourceId(prefId: Int) = QuickButtonIcon.entries
    .find {
        it.prefId == prefId
    }?.resourceId

@FragmentScoped
class QuickButtonPreferencesRepository @Inject constructor(
    quickButtonPreferencesStore: DataStore<QuickButtonPreferences>,
    lifecycleScope: CoroutineScope,
    lifecycleOwnerSupplier: LifecycleOwnerSupplier
) : AbstractDataRepository<QuickButtonPreferences>(
    quickButtonPreferencesStore,
    lifecycleScope,
    lifecycleOwnerSupplier,
    QuickButtonPreferences::getDefaultInstance
)

object QuickButtonPreferencesSerializer : AbstractDataSerializer<QuickButtonPreferences>(
    QuickButtonPreferences::getDefaultInstance,
    QuickButtonPreferences::parseFrom
)
