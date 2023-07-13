package com.sduduzog.slimlauncher.datasource.quickbuttonprefs

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import com.jkuester.unlauncher.datastore.QuickButtonPreferences

class QuickButtonPreferencesMigrations {
    fun get(context: Context): List<DataMigration<QuickButtonPreferences>> {
        return listOf(
            SharedPreferencesMigration(
                context,
                "settings",
                setOf("quick_button_left", "quick_button_center", "quick_button_right")
            ) { sharedPrefs: SharedPreferencesView, currentData: QuickButtonPreferences ->
                val prefBuilder = currentData.toBuilder()
                if (!currentData.hasLeftButton()) {
                    prefBuilder.leftButton =
                        QuickButtonPreferences.QuickButton.newBuilder().setIconId(
                            sharedPrefs.getInt(
                                "quick_button_left",
                                QuickButtonPreferencesRepository.IC_CALL
                            )
                        ).build()
                }

                if (!currentData.hasCenterButton()) {
                    prefBuilder.centerButton =
                        QuickButtonPreferences.QuickButton.newBuilder().setIconId(
                            sharedPrefs.getInt(
                                "quick_button_center",
                                QuickButtonPreferencesRepository.IC_COG
                            )
                        ).build()
                }
                if (!currentData.hasRightButton()) {
                    prefBuilder.rightButton =
                        QuickButtonPreferences.QuickButton.newBuilder().setIconId(
                            sharedPrefs.getInt(
                                "quick_button_right",
                                QuickButtonPreferencesRepository.IC_PHOTO_CAMERA
                            )
                        ).build()
                }
                prefBuilder.build()
            },
            object : DataMigration<QuickButtonPreferences> {
                override suspend fun shouldMigrate(currentData: QuickButtonPreferences): Boolean {
                    return !QuickButtonPreferencesRepository.RES_BY_ICON.keys.containsAll(
                        listOf(
                            currentData.leftButton.iconId,
                            currentData.centerButton.iconId,
                            currentData.rightButton.iconId
                        )
                    )
                }

                override suspend fun migrate(currentData: QuickButtonPreferences): QuickButtonPreferences {
                    val icons = QuickButtonPreferencesRepository.RES_BY_ICON.keys
                    val prefBuilder = currentData.toBuilder()
                    if (!icons.contains(currentData.leftButton.iconId)) {
                        prefBuilder.leftButton = QuickButtonPreferences.QuickButton.newBuilder()
                            .setIconId(QuickButtonPreferencesRepository.IC_CALL).build()
                    }
                    if (!icons.contains(currentData.centerButton.iconId)) {
                        prefBuilder.centerButton = QuickButtonPreferences.QuickButton.newBuilder()
                            .setIconId(QuickButtonPreferencesRepository.IC_COG).build()
                    }
                    if (!icons.contains(currentData.rightButton.iconId)) {
                        prefBuilder.rightButton = QuickButtonPreferences.QuickButton.newBuilder()
                            .setIconId(QuickButtonPreferencesRepository.IC_PHOTO_CAMERA).build()
                    }
                    return prefBuilder.build()
                }

                override suspend fun cleanUp() {}
            }
        )
    }
}