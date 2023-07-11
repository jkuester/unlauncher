package com.sduduzog.slimlauncher.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.sduduzog.slimlauncher.R
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SystemUiManager @Inject constructor(@ActivityContext private val context: Context) {
    private val window: Window = (context as Activity).window
    private val settings: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.prefs_settings),
        AppCompatActivity.MODE_PRIVATE
    )

    fun setSystemUiVisibility() {
        window.decorView.systemUiVisibility =
            getLightUiBarFlags() or getToggleStatusBarFlags()
    }

    fun setSystemUiColors() {
        val primaryColor = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = primaryColor.data
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = primaryColor.data
        }
    }

    private fun getToggleStatusBarFlags(): Int {
        val isHidden = settings.getBoolean(
            context.getString(R.string.prefs_settings_key_toggle_status_bar),
            false
        )
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE or if (isHidden) View.SYSTEM_UI_FLAG_FULLSCREEN else 0
    }

    private fun getLightUiBarFlags(): Int {
        val theme = settings.getInt(context.getString(R.string.prefs_settings_key_theme), 0)
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        var uiFlags = 0
        if (listOf(
                6, 3, 5
            ).contains(theme) || (theme == 0 && uiMode == Configuration.UI_MODE_NIGHT_NO)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                uiFlags = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        return uiFlags
    }
}
