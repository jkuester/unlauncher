package com.sduduzog.slimlauncher.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sduduzog.slimlauncher.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
open class SystemUiManager internal constructor(internal val context: Context) {
    internal val window: Window = (context as Activity).window
    internal val settings: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.prefs_settings),
        AppCompatActivity.MODE_PRIVATE
    )

    companion object {
        @Provides
        fun createInstance(@ActivityContext context: Context): SystemUiManager {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (context as Activity).window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return LSystemUiManager(context)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return MSystemUiManager(context)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                return OSystemUiManager(context)
            }
            return SystemUiManager(context)
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    open fun setSystemUiVisibility() {
        val insetsController = window.insetsController

        if (isSystemUiHidden()) {
            insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            insetsController?.show(WindowInsets.Type.statusBars())
        }

        if (isLightModeTheme()) {
            insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    open fun setSystemUiColors() {
        // These colors can be hard-coded in the theme xml once the minimum Android API version is 26
        val primaryColor = getPrimaryColor()
        window.statusBarColor = primaryColor
        window.navigationBarColor = primaryColor
    }

    internal fun getPrimaryColor(): Int {
        val primaryColor = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
        return primaryColor.data
    }

    internal fun isSystemUiHidden(): Boolean {
        return settings.getBoolean(
            context.getString(R.string.prefs_settings_key_toggle_status_bar),
            false
        )
    }

    internal fun isLightModeTheme(): Boolean {
        val theme = settings.getInt(context.getString(R.string.prefs_settings_key_theme), 0)
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return listOf(
            6,
            3,
            5
        ).contains(theme) || (theme == 0 && uiMode == Configuration.UI_MODE_NIGHT_NO)
    }

    private open class OSystemUiManager(context: Context) : SystemUiManager(context) {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun setSystemUiVisibility() {
            window.decorView.systemUiVisibility =
                getLightUiBarFlags() or getToggleStatusBarFlags()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        open fun getLightUiBarFlags(): Int {
            return if (isLightModeTheme()) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                0
            }
        }

        private fun getToggleStatusBarFlags(): Int {
            return View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                if (isSystemUiHidden()) View.SYSTEM_UI_FLAG_FULLSCREEN else 0
        }
    }

    private open class MSystemUiManager(context: Context) : OSystemUiManager(context) {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun setSystemUiColors() {
            window.statusBarColor = getPrimaryColor()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getLightUiBarFlags(): Int {
            return if (isLightModeTheme()) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
        }
    }

    private class LSystemUiManager(context: Context) : MSystemUiManager(context) {
        override fun setSystemUiColors() {}

        override fun getLightUiBarFlags(): Int {
            return 0
        }
    }
}
