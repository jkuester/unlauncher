package com.sduduzog.slimlauncher.utils

import android.app.Activity
import android.app.WallpaperManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.WithActivityLifecycle
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.MainActivity
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperManager @Inject constructor(
    activity: Activity,
    @WithActivityLifecycle private val corePreferencesRepo: DataRepository<CorePreferences>
) {
    private val mainActivity = (activity as MainActivity)

    fun onApplyThemeResource(theme: Resources.Theme?, @StyleRes resid: Int) {
        if (!isActivityDefaultLauncher(mainActivity)) {
            return
        }
        corePreferencesRepo.observe {
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
        val array = theme?.obtainStyledAttributes(
            themeRes,
            intArrayOf(android.R.attr.colorBackground)
        )
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
