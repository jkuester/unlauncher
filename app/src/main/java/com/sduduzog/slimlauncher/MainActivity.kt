package com.sduduzog.slimlauncher

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.datastore.core.DataStore
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.HomeWatcher
import com.sduduzog.slimlauncher.utils.IPublisher
import com.sduduzog.slimlauncher.utils.ISubscriber
import com.sduduzog.slimlauncher.utils.SystemUiManager
import com.sduduzog.slimlauncher.utils.WallpaperManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import java.lang.reflect.Method
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    HomeWatcher.OnHomePressedListener,
    IPublisher {

    @Inject
    lateinit var systemUiManager: SystemUiManager

    @Inject
    lateinit var corePreferencesStore: DataStore<CorePreferences>

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface WallpaperManagerFactory {
        fun getWallpaperManager(): WallpaperManager
    }

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
        if (s.equals(getString(R.string.prefs_settings_key_theme), true)) {
            recreate()
        }
        if (s.equals(getString(R.string.prefs_settings_key_toggle_status_bar), true)) {
            systemUiManager.setSystemUiVisibility()
        }
    }

    override fun onApplyThemeResource(theme: Resources.Theme?, @StyleRes resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)
        // This function is called too early in the lifecycle for normal injection so we do it the hard way
        val factory = EntryPointAccessors.fromActivity(this, WallpaperManagerFactory::class.java)
        val wallpaperManager = factory.getWallpaperManager()
        wallpaperManager.onApplyThemeResource(theme, resid)
    }

    override fun setTheme(resId: Int) {
        super.setTheme(getUserSelectedThemeRes())
    }

    @StyleRes
    fun getUserSelectedThemeRes(): Int {
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        val active = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)
        return resolveTheme(active)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        dispatchBack()
    }

    override fun onHomePressed() {
        dispatchHome()
        navigator.popBackStack(R.id.homeFragment, false)
    }

    companion object {
        @StyleRes
        fun resolveTheme(i: Int): Int = when (i) {
            1 -> R.style.AppThemeDark
            2 -> R.style.AppGreyTheme
            3 -> R.style.AppTealTheme
            4 -> R.style.AppCandyTheme
            5 -> R.style.AppPinkTheme
            6 -> R.style.AppThemeLight
            7 -> R.style.AppDarculaTheme
            8 -> R.style.AppGruvBoxDarkTheme
            9 -> R.style.LightWallpaper
            10 -> R.style.DarkWallpaper
            else -> R.style.AppTheme
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

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
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
