package com.jkuester.unlauncher.datasource

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import com.jkuester.unlauncher.datasource.SharedPrefButton.CENTER
import com.jkuester.unlauncher.datasource.SharedPrefButton.LEFT
import com.jkuester.unlauncher.datasource.SharedPrefButton.RIGHT
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences

private const val SHARED_PREF_GROUP_NAME = "settings"
private enum class SharedPrefButton(val key: String) {
    LEFT("quick_button_left"),
    CENTER("quick_button_center"),
    RIGHT("quick_button_right")
}

private fun populateLeftButton(sharedPrefs: SharedPreferencesView) =
    fun(currentData: QuickButtonPreferences) = when {
        currentData.hasLeftButton() -> currentData
        else -> sharedPrefs
            .getInt(LEFT.key, QuickButtonIcon.IC_CALL.prefId)
            .let { setLeftIconId(it)(currentData) }
    }
private fun populateCenterButton(sharedPrefs: SharedPreferencesView) =
    fun(currentData: QuickButtonPreferences) = when {
        currentData.hasCenterButton() -> currentData
        else -> sharedPrefs
            .getInt(CENTER.key, QuickButtonIcon.IC_COG.prefId)
            .let { setCenterIconId(it)(currentData) }
    }
private fun populateRightButton(sharedPrefs: SharedPreferencesView) =
    fun(currentData: QuickButtonPreferences) = when {
        currentData.hasRightButton() -> currentData
        else -> sharedPrefs
            .getInt(RIGHT.key, QuickButtonIcon.IC_PHOTO_CAMERA.prefId)
            .let { setRightIconId(it)(currentData) }
    }

fun sharedPrefsMigration(context: Context) = SharedPreferencesMigration(
    context,
    SHARED_PREF_GROUP_NAME,
    SharedPrefButton.entries
        .map { it.key }
        .toSet()
) { sharedPrefs: SharedPreferencesView, currentData: QuickButtonPreferences ->
    currentData
        .let(populateLeftButton(sharedPrefs))
        .let(populateCenterButton(sharedPrefs))
        .let(populateRightButton(sharedPrefs))
}

private fun getButtonPrefIds() = QuickButtonIcon.entries.map { it.prefId }

private fun defaultLeftButton(currentData: QuickButtonPreferences) =
    if (getButtonPrefIds().contains(currentData.leftButton.iconId)) {
        currentData
    } else {
        setLeftIconId(QuickButtonIcon.IC_CALL.prefId)(currentData)
    }
private fun defaultCenterButton(currentData: QuickButtonPreferences) =
    if (getButtonPrefIds().contains(currentData.centerButton.iconId)) {
        currentData
    } else {
        setCenterIconId(QuickButtonIcon.IC_COG.prefId)(currentData)
    }
private fun defaultRightButton(currentData: QuickButtonPreferences) =
    if (getButtonPrefIds().contains(currentData.rightButton.iconId)) {
        currentData
    } else {
        setRightIconId(QuickButtonIcon.IC_PHOTO_CAMERA.prefId)(currentData)
    }

object ToThreeQuickButtonsMigration : DataMigration<QuickButtonPreferences> {
    override suspend fun shouldMigrate(currentData: QuickButtonPreferences): Boolean =
        !getButtonPrefIds()
            .containsAll(listOf(
                currentData.leftButton.iconId,
                currentData.centerButton.iconId,
                currentData.rightButton.iconId
            ))

    override suspend fun migrate(currentData: QuickButtonPreferences): QuickButtonPreferences =
        currentData
            .let(::defaultLeftButton)
            .let(::defaultCenterButton)
            .let(::defaultRightButton)

    override suspend fun cleanUp() {}
}
