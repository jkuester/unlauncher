package com.sduduzog.slimlauncher

import android.app.WallpaperManager
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.sduduzog.slimlauncher.di.MainFragmentFactoryEntryPoint
import com.sduduzog.slimlauncher.utils.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.io.IOException
import kotlin.jvm.Throws


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    HomeWatcher.OnHomePressedListener, IPublisher {

    private lateinit var settings: SharedPreferences
    private lateinit var navigator: NavController
    private lateinit var homeWatcher: HomeWatcher
    private val subscribers: MutableSet<BaseFragment> = mutableSetOf()

    override fun attachSubscriber(s: ISubscriber) {
        subscribers.add(s as BaseFragment)
    }

    override fun detachSubscriber(s: ISubscriber) {
        subscribers.remove(s as BaseFragment)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun dispatchBack() {
        for (s in subscribers) if (s.onBack()) return
        completeBackAction()
    }

    private fun dispatchHome() {
        for (s in subscribers) s.onHome()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint = EntryPointAccessors.fromActivity(this, MainFragmentFactoryEntryPoint::class.java)
        supportFragmentManager.fragmentFactory = entryPoint.getMainFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        settings.registerOnSharedPreferenceChangeListener(this)
        navigator = findNavController(this, R.id.nav_host_fragment)
        homeWatcher = HomeWatcher(this)
        homeWatcher.setOnHomePressedListener(this)
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        toggleStatusBar()
    }

    override fun onStart() {
        super.onStart()
        homeWatcher.startWatch()
    }

    override fun onStop() {
        super.onStop()
        homeWatcher.stopWatch()
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) toggleStatusBar()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (s.equals(getString(R.string.prefs_settings_key_theme), true)) {
            recreate()
        }
        if (s.equals(getString(R.string.prefs_settings_key_toggle_status_bar), true)) {
            toggleStatusBar()
        }
    }

    override fun onApplyThemeResource(theme: Resources.Theme?, @StyleRes resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)
        if (!first) {
            return
        }
        @ColorInt val backgroundColor = getThemeBackgroundColor(theme, resid)
        if (getWallpaperBackgroundColor() != backgroundColor) {
            try {
                setWallpaperBackgroundColor(backgroundColor)
            } catch (e: IOException) {
                // do nothing
            }
        }
    }

    @ColorInt
    private fun getThemeBackgroundColor(theme: Resources.Theme?, @StyleRes themeRes: Int): Int {
        val array =  theme?.obtainStyledAttributes(themeRes, intArrayOf(android.R.attr.colorBackground))
        try {
            return array?.getColor(0, 0) ?: 0
        } finally {
            array?.recycle()
        }
    }

    @ColorInt
    private fun getWallpaperBackgroundColor(): Int {
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val drawable = wallpaperManager.drawable
        if (drawable !is BitmapDrawable) {
            // user might have a live wallpaper set
            return 0
        }
        // only retrieving the top left since the wallpaper consists of a single color
        return drawable.bitmap[0, 0]
    }

    @Throws(IOException::class)
    private fun setWallpaperBackgroundColor(@ColorInt color: Int) {
        val wallpaperBitmap = createColoredWallpaperBitmap(color)
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        wallpaperManager.setBitmap(wallpaperBitmap)
    }

    private fun createColoredWallpaperBitmap(@ColorInt color: Int): Bitmap {
        val width = getScreenWidth(this)
        val height = getScreenHeight(this)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }



    override fun setTheme(resId: Int) {
        val userThemeId = getUserSelectedThemeRes()
        val id = if (resId != userThemeId) {
            userThemeId
        } else {
            resId
        }
        super.setTheme(id)
    }

    @StyleRes
    private fun getUserSelectedThemeRes(): Int {
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        val active = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)
        return resolveTheme(active)
    }

    override fun onBackPressed() {
        dispatchBack()
    }

    override fun onHomePressed() {
        dispatchHome()
        navigator.popBackStack(R.id.homeFragment, false)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun toggleStatusBar() {
        val isHidden = settings.getBoolean(getString(R.string.prefs_settings_key_toggle_status_bar), false)
        if (isHidden) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    companion object {

        @StyleRes
        fun resolveTheme(i: Int): Int {
            return when (i) {
                1 -> R.style.AppDarkTheme
                2 -> R.style.AppGreyTheme
                3 -> R.style.AppTealTheme
                4 -> R.style.AppCandyTheme
                5 -> R.style.AppPinkTheme
                else -> R.style.AppTheme
            }
        }
    }

    private fun completeBackAction() {
        super.onBackPressed()
    }

    private val gestureDetector = GestureDetector(baseContext, object : SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            // Open Options
            val homeView = findViewById<View>(R.id.home_fragment)
            if(homeView != null) {
                findNavController(homeView).navigate(R.id.action_homeFragment_to_optionsFragment, null)
            }
        }
    })
}
