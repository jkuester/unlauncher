package com.sduduzog.slimlauncher

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.sduduzog.slimlauncher.data.Constants
import com.sduduzog.slimlauncher.di.MainFragmentFactoryEntryPoint
import com.sduduzog.slimlauncher.utils.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.android.synthetic.main.main_activity.*
import java.lang.Exception


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        HomeWatcher.OnHomePressedListener, IPublisher {

    private lateinit var settings: SharedPreferences
    private lateinit var navigator: NavController
    private lateinit var homeWatcher: HomeWatcher
    private lateinit var deviceManager: DevicePolicyManager
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
        deviceManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        toggleStatusBar()
    }

    override fun onStart() {
        super.onStart()
        homeWatcher.startWatch()
        lock.setOnClickListener { /* nothing to do*/ }
    }

    override fun onStop() {
        super.onStop()
        homeWatcher.stopWatch()
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

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        settings = getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
        val active = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)
        theme.applyStyle(resolveTheme(active), true)
        return theme
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

    private fun handleDoubleTap() {
        // Code taken from OLauncher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (isAccessServiceEnabled()) {
                lock.performClick()
                Log.d(MainActivity::class.java.name, "Lock!")
            } else {
                Log.d(MainActivity::class.java.name, "Not enabled!")
            }
        } else {
            lockPhone()
        }
    }

    private fun lockPhone() {
        try {
            deviceManager.lockNow()
        } catch (e: SecurityException) {
            Log.d(MainActivity::class.java.name, "Security!")
        } catch (e: Exception) {
            Log.d(MainActivity::class.java.name, "Regular!")
        }
    }

    private fun isAccessServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getInt(applicationContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        if (enabled == 1) {
            val prefString = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return prefString.contains(packageName + "/" + LockDeviceAccessibilityService::class.java.name)
        }
        return false
    }

    private val gestureDetector = GestureDetector(baseContext, object : SimpleOnGestureListener() {

        val handler = Handler(Looper.getMainLooper())

        override fun onLongPress(e: MotionEvent) {
            // Open Options
            val homeView = findViewById<View>(R.id.home_fragment)
            if(homeView != null) {
                findNavController(homeView).navigate(R.id.action_homeFragment_to_optionsFragment, null)
            }
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            handler.postDelayed({
                handleDoubleTap()
            }, Constants.LONG_PRESS_DELAY_MS.toLong())
            return super.onDoubleTap(e)
        }
    })
}
