package com.sduduzog.slimlauncher.utils

import android.app.WallpaperManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.sduduzog.slimlauncher.MainActivity
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class WallpaperManager(private val mainActivity: MainActivity) {
    fun onApplyThemeResource(theme: Resources.Theme?, @StyleRes resid: Int) {
        if(!isActivityDefaultLauncher(mainActivity)) {
            return
        }
        // Cannot inject here because this is called too early in the lifecycle
        val unlauncherDataSource = UnlauncherDataSource(mainActivity, mainActivity.lifecycleScope)
        unlauncherDataSource.corePreferencesRepo.liveData().observe(mainActivity) {
            if (it.keepDeviceWallpaper && mainActivity.getUserSelectedThemeRes() == resid) {
                // only change the wallpaper when user has allowed it and
                // preventing to change the wallpaper multiple times once it is rechecked in the settings
                return@observe
            }
            @ColorInt val backgroundColor = getThemeBackgroundColor(theme, resid)
            if (backgroundColor == Int.MIN_VALUE) {
                return@observe
            }
            mainActivity.lifecycleScope.launch(Dispatchers.IO) {
                setWallpaperBackgroundColor(backgroundColor)
            }
        }
    }

    /**
     * @return `Int.MIN_VALUE` if `android.R.attr.colorBackground` of `theme` could not be obtained.
     */
    @ColorInt
    private fun getThemeBackgroundColor(theme: Resources.Theme?, @StyleRes themeRes: Int): Int {
        val array =  theme?.obtainStyledAttributes(themeRes, intArrayOf(android.R.attr.colorBackground))
        try {
            return array?.getColor(0, Int.MIN_VALUE) ?: Int.MIN_VALUE
        } finally {
            array?.recycle()
        }
    }

    @Throws(IOException::class)
    @WorkerThread
    private fun setWallpaperBackgroundColor(@ColorInt color: Int) {
        val wallpaperManager = WallpaperManager.getInstance(mainActivity)
        var width = wallpaperManager.desiredMinimumWidth
        if (width <= 0) {
            width = getScreenWidth(mainActivity)
        }
        var height = wallpaperManager.desiredMinimumHeight
        if (height <= 0) {
            height = getScreenHeight(mainActivity)
        }
        val wallpaperBitmap = createColoredWallpaperBitmap(color, width, height)
        wallpaperManager.setBitmap(wallpaperBitmap)
    }

    private fun createColoredWallpaperBitmap(@ColorInt color: Int, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }
}