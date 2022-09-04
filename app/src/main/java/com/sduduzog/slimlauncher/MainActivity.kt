package com.sduduzog.slimlauncher

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.di.MainFragmentFactoryEntryPoint
import com.sduduzog.slimlauncher.utils.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.reflect.Method


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    HomeWatcher.OnHomePressedListener, IPublisher {

    private lateinit var settings: SharedPreferences
    private lateinit var navigator: NavController
    private lateinit var homeWatcher: HomeWatcher
    private lateinit var unlauncherDataSource: UnlauncherDataSource

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
        getUnlaucherDataSource().unlauncherAppsRepo.liveData().observe(this, {
            if (!it.setThemeWallpaper && getUserSelectedThemeRes() == resid) {
                // only change the wallpaper when user has allowed it and
                // preventing to change the wallpaper multiple times once it is rechecked in the settings
                return@observe
            }
            @ColorInt val backgroundColor = getThemeBackgroundColor(theme, resid)
            if (backgroundColor == Int.MIN_VALUE) {
                return@observe
            }
            lifecycleScope.launch(Dispatchers.IO) {
                setWallpaperBackgroundColor(backgroundColor)
            }
        })
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
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        var width = wallpaperManager.desiredMinimumWidth
        if (width <= 0) {
            width = getScreenWidth(this)
        }
        var height = wallpaperManager.desiredMinimumHeight
        if (height <= 0) {
            height = getScreenHeight(this)
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

    private fun getUnlaucherDataSource(): UnlauncherDataSource {
        if (!::unlauncherDataSource.isInitialized) {
            unlauncherDataSource = UnlauncherDataSource(this, lifecycleScope)
        }
        return unlauncherDataSource
    }

    companion object {

        @StyleRes
        fun resolveTheme(i: Int): Int {
            return when (i) {
                1 -> R.style.AppThemeDark
                2 -> R.style.AppGreyTheme
                3 -> R.style.AppTealTheme
                4 -> R.style.AppCandyTheme
                5 -> R.style.AppPinkTheme
                6 -> R.style.AppThemeLight
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

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val homeView = findViewById<MotionLayout>(R.id.home_fragment)
            if (homeView != null) {
                val homeScreen = homeView.constraintSetIds[0]
                val isFlingFromHomeScreen = homeView.currentState == homeScreen
                val isFlingDown = velocityY > 0 && velocityY > velocityX.absoluteValue
                if (isFlingDown && isFlingFromHomeScreen) {
                    expandStatusBar()
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    })

    @SuppressLint("WrongConstant")  // statusbar is an internal API
    private fun expandStatusBar() {
        try {
            getSystemService("statusbar")?.let { service ->
                val statusbarManager = Class.forName("android.app.StatusBarManager")
                val expand: Method = statusbarManager.getMethod("expandNotificationsPanel")
                expand.invoke(service)
            }
        } catch (e: Exception) {
            // Do nothing. There does not seem to be any official way with the Android SKD to open the status bar.
            // https://stackoverflow.com/questions/5029354/how-can-i-programmatically-open-close-notifications-in-android
            // This hack may break on future versions of Android (or even just not work for specific manufacturer variants).
            // So, if anything goes wrong, we will just do nothing.
            Log.e(
                "MainActivity",
                "Error trying to expand the notifications panel.",
                e
            )
        }
    }
}
