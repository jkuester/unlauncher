package com.sduduzog.slimlauncher

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.HomeWatcher
import com.sduduzog.slimlauncher.utils.IPublisher
import com.sduduzog.slimlauncher.utils.ISubscriber
import com.sduduzog.slimlauncher.utils.SystemUiManager
import com.sduduzog.slimlauncher.utils.WallpaperManager
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Method
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    HomeWatcher.OnHomePressedListener,
    IPublisher {

    private val wallpaperManager = WallpaperManager(this)

    @Inject
    lateinit var systemUiManager: SystemUiManager
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        settings.registerOnSharedPreferenceChangeListener(this)
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        navigator = navHostFragment.navController
        homeWatcher = HomeWatcher.createInstance(this)
        homeWatcher.setOnHomePressedListener(this)
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        systemUiManager.setSystemUiVisibility()
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
        if (hasFocus) systemUiManager.setSystemUiVisibility()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (
            s.equals(getString(R.string.prefs_settings_key_theme), true) ||
            s.equals(getString(R.string.prefs_settings_key_dark_mode), true) ||
            s.equals(getString(R.string.prefs_settings_key_font), true)
        ) {
            recreate()
        }
        if (s.equals(getString(R.string.prefs_settings_key_toggle_status_bar), true)) {
            systemUiManager.setSystemUiVisibility()
        }
    }

    override fun onApplyThemeResource(
        theme: Resources.Theme?,
        @StyleRes resid: Int,
        first: Boolean
    ) {
        super.onApplyThemeResource(theme, resid, first)
        wallpaperManager.onApplyThemeResource(theme, resid)
    }

    override fun setTheme(resId: Int) {
        super.setTheme(getUserSelectedThemeRes())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            AppCompatDelegate.setDefaultNightMode(
                when (getDarkMode()) {
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    2 -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        } else {
            val uimm = getSystemService(UI_MODE_SERVICE) as UiModeManager
            uimm.setApplicationNightMode(
                when (getDarkMode()) {
                    1 -> UiModeManager.MODE_NIGHT_YES
                    2 -> UiModeManager.MODE_NIGHT_NO
                    else -> UiModeManager.MODE_NIGHT_AUTO
                }
            )
        }
    }

    @StyleRes
    fun getUserSelectedThemeRes(): Int {
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        val themeId = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)
        val fontId = settings.getInt(getString(R.string.prefs_settings_key_font), 0)
        return resolveTheme(themeId, fontId)
    }

    fun getDarkMode(): Int {
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        return settings.getInt(getString(R.string.prefs_settings_key_dark_mode), 0)
    }

    override fun onBackPressed() {
        dispatchBack()
    }

    override fun onHomePressed() {
        dispatchHome()
        navigator.popBackStack(R.id.homeFragment, false)
    }

    companion object {
        @StyleRes
        fun resolveTheme(theme: Int, font: Int): Int {
            return when (theme) {
                1 -> when (font) {
                    1 -> R.style.AppThemeDark
                    2 -> R.style.AppThemeDarkSerif
                    3 -> R.style.AppThemeDarkMono
                    4 -> R.style.AppThemeDarkCursive
                    else -> R.style.AppThemeDarkUbuntu
                }
                2 -> when (font) {
                    1 -> R.style.AppGreyTheme
                    2 -> R.style.AppGreyThemeSerif
                    3 -> R.style.AppGreyThemeMono
                    4 -> R.style.AppGreyThemeCursive
                    else -> R.style.AppGreyThemeUbuntu
                }
                3 -> when (font) {
                    1 -> R.style.AppTealTheme
                    2 -> R.style.AppTealThemeSerif
                    3 -> R.style.AppTealThemeMono
                    4 -> R.style.AppTealThemeCursive
                    else -> R.style.AppTealThemeUbuntu
                }
                4 -> when (font) {
                    1 -> R.style.AppCandyTheme
                    2 -> R.style.AppCandyThemeSerif
                    3 -> R.style.AppCandyThemeMono
                    4 -> R.style.AppCandyThemeCursive
                    else -> R.style.AppCandyThemeUbuntu
                }
                5 -> when (font) {
                    1 -> R.style.AppPinkTheme
                    2 -> R.style.AppPinkThemeSerif
                    3 -> R.style.AppPinkThemeMono
                    4 -> R.style.AppPinkThemeCursive
                    else -> R.style.AppPinkThemeUbuntu
                }
                6 -> when (font) {
                    1 -> R.style.AppThemeLight
                    2 -> R.style.AppThemeLightSerif
                    3 -> R.style.AppThemeLightMono
                    4 -> R.style.AppThemeLightCursive
                    else -> R.style.AppThemeLightUbuntu
                }
                7 -> when (font) {
                    1 -> R.style.AppDarculaTheme
                    2 -> R.style.AppDarculaThemeSerif
                    3 -> R.style.AppDarculaThemeMono
                    4 -> R.style.AppDarculaThemeCursive
                    else -> R.style.AppDarculaThemeUbuntu
                }
                8 -> when (font) {
                    1 -> R.style.AppGruvBoxDarkTheme
                    2 -> R.style.AppGruvBoxDarkThemeSerif
                    3 -> R.style.AppGruvBoxDarkThemeMono
                    4 -> R.style.AppGruvBoxDarkThemeCursive
                    else -> R.style.AppGruvBoxDarkThemeUbuntu
                }
                9 -> when (font) {
                    1 -> R.style.AppBlackOrangeTheme
                    2 -> R.style.AppBlackOrangeThemeSerif
                    3 -> R.style.AppBlackOrangeThemeMono
                    4 -> R.style.AppBlackOrangeThemeCursive
                    else -> R.style.AppBlackOrangeThemeUbuntu
                }
                10 -> when (font) {
                    1 -> R.style.AppBlackRedTheme
                    2 -> R.style.AppBlackRedThemeSerif
                    3 -> R.style.AppBlackRedThemeMono
                    4 -> R.style.AppBlackRedThemeCursive
                    else -> R.style.AppBlackRedThemeUbuntu
                }
                11 -> when (font) {
                    1 -> R.style.AppBlackCyanTheme
                    2 -> R.style.AppBlackCyanThemeSerif
                    3 -> R.style.AppBlackCyanThemeMono
                    4 -> R.style.AppBlackCyanThemeCursive
                    else -> R.style.AppBlackCyanThemeUbuntu
                }
                12 -> when (font) {
                    1 -> R.style.AppBlackBlueTheme
                    2 -> R.style.AppBlackBlueThemeSerif
                    3 -> R.style.AppBlackBlueThemeMono
                    4 -> R.style.AppBlackBlueThemeCursive
                    else -> R.style.AppBlackBlueThemeUbuntu
                }
                else -> when (font) {
                    1 -> R.style.AppTheme
                    2 -> R.style.AppThemeSerif
                    3 -> R.style.AppThemeMono
                    4 -> R.style.AppThemeCursive
                    else -> R.style.AppThemeUbuntu
                }
            }
        }
    }

    private fun completeBackAction() {
        super.onBackPressed()
    }

    private fun isVisible(view: View): Boolean {
        if (!view.isShown) {
            return false
        }

        val actualPosition = Rect()
        view.getGlobalVisibleRect(actualPosition)
        val screen = Rect(
            0,
            0,
            Resources.getSystem().displayMetrics.widthPixels,
            Resources.getSystem().displayMetrics.heightPixels
        )
        return actualPosition.intersect(screen)
    }

    private val gestureDetector = GestureDetector(
        baseContext,
        object : SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                // Open Options
                val recyclerView = findViewById<RecyclerView>(R.id.app_drawer_fragment_list)
                val homeView = findViewById<View>(R.id.home_fragment)

                if (homeView != null && recyclerView != null) {
                    if (isVisible(recyclerView)) {
                        recyclerView.performLongClick()
                    } else {
                        // we are in the homeFragment
                        findNavController(
                            homeView
                        ).navigate(R.id.action_homeFragment_to_optionsFragment, null)
                    }
                }
            }

            override fun onFling(
                e1: MotionEvent?,
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
        }
    )

    @SuppressLint("WrongConstant") // statusbar is an internal API
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
