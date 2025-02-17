package com.jkuester.unlauncher.datasource

import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences.QuickButton
import com.sduduzog.slimlauncher.R

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

object QuickButtonPreferencesSerializer : AbstractDataSerializer<QuickButtonPreferences>(
    QuickButtonPreferences::getDefaultInstance,
    QuickButtonPreferences::parseFrom
)
