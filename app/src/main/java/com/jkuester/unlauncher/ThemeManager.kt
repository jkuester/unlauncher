package com.jkuester.unlauncher

import android.app.Activity
import android.app.WallpaperManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Canvas
import android.os.Build
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.datastore.core.DataStore
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.getThemeStyleResource
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.Theme
import com.sduduzog.slimlauncher.utils.isDefaultLauncher
import kotlinx.coroutines.flow.first

private fun getScreenResolution(activity: Activity) = if (androidSdkAtLeast(Build.VERSION_CODES.R)) {
    val bounds = activity.windowManager.currentWindowMetrics.bounds
    Pair(bounds.width(), bounds.height())
} else {
    val metrics = DisplayMetrics()
        .also(activity.windowManager.defaultDisplay::getMetrics)
    Pair(metrics.widthPixels, metrics.heightPixels)
}

private fun createColoredWallpaperBitmap(color: Int, width: Int, height: Int) = createBitmap(width, height)
    .also { Canvas(it).drawColor(color) }

private fun setWallpaperBackgroundColor(activity: Activity) = { color: Int ->
    WallpaperManager
        .getInstance(activity)
        .run {
            val screenRes = getScreenResolution(activity)
            val width = desiredMinimumWidth.takeIf { it > 0 } ?: screenRes.first
            val height = desiredMinimumHeight.takeIf { it > 0 } ?: screenRes.second
            val wallpaperBitmap = createColoredWallpaperBitmap(color, width, height)
            setBitmap(wallpaperBitmap)
        }
}

private fun getThemeBackgroundColor(theme: Resources.Theme, themeRes: Int): Int {
    val typedArray = theme.obtainStyledAttributes(themeRes, intArrayOf(android.R.attr.colorBackground))
    return try {
        typedArray.getColor(0, Int.MIN_VALUE)
    } finally {
        typedArray.recycle()
    }
}

private suspend fun setWallpaper(
    activity: AppCompatActivity,
    corePrefsStore: DataStore<CorePreferences>,
    theme: Resources.Theme,
    resId: Int
) {
    val corePrefs = corePrefsStore.data.first()
    if (corePrefs.keepDeviceWallpaper || !isDefaultLauncher(activity)) {
        return
    }

    getThemeBackgroundColor(theme, resId)
        .takeUnless { it == Int.MIN_VALUE }
        ?.let(setWallpaperBackgroundColor(activity))
}

private fun isDarkTheme(configuration: Configuration): Boolean =
    configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

class ThemeManager(private val activity: AppCompatActivity) {
    private var darkModeStatus: Boolean? = null
    private lateinit var currentTheme: Theme

    suspend fun setDeviceWallpaper(
        corePrefsStore: DataStore<CorePreferences>,
        theme: Resources.Theme?,
        resId: Int,
        first: Boolean
    ) {
        // first is true when starting the app (theme has not actually changed)
        if (theme == null || (first && !this.darkModeChanged())) {
            return
        }
        setWallpaper(activity, corePrefsStore, theme, resId)
    }

    fun listenForThemeChanges(corePrefRepo: DataRepository<CorePreferences>, initialTheme: Theme) {
        currentTheme = initialTheme
        corePrefRepo.observe {
            if (it.theme == currentTheme) {
                return@observe
            }

            currentTheme = it.theme
            activity.setTheme(getThemeStyleResource(currentTheme))
            activity.recreate()
        }
    }

    private fun darkModeChanged(): Boolean {
        val originalStatus = darkModeStatus
        darkModeStatus = isDarkTheme(activity.resources.configuration)
        return originalStatus != null && originalStatus != darkModeStatus
    }
}
