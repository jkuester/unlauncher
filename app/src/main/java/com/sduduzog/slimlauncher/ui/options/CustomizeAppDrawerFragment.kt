package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.isAppDefaultLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.*

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment() {

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        customize_app_drawer_fragment_visible_apps
            .setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment))

        val unlauncherAppsRepo = getUnlauncherDataSource().unlauncherAppsRepo
        setupKeyboardSwitch(unlauncherAppsRepo)
        setupAutomaticDeviceWallpaperSwitch(unlauncherAppsRepo)

    }

    private fun setupKeyboardSwitch(appsRepo: UnlauncherAppsRepository) {
        customize_app_drawer_open_keyboard_switch.setOnCheckedChangeListener { _, checked ->
            appsRepo.updateActivateKeyboardInDrawer(checked)
        }
        appsRepo.liveData().observe(viewLifecycleOwner) {
            customize_app_drawer_open_keyboard_switch.isChecked = it.activateKeyboardInDrawer
        }
    }

    private fun setupAutomaticDeviceWallpaperSwitch(appsRepo: UnlauncherAppsRepository) {
        setupAutomaticDeviceWallpaperSwitchText()
        val appIsDefaultLauncher = isAppDefaultLauncher(requireContext())
        if (!appIsDefaultLauncher) {
            // always uncheck once app isn't default launcher
            customize_app_drawer_auto_device_theme_wallpaper.isChecked = false
        }
        customize_app_drawer_auto_device_theme_wallpaper.isEnabled = appIsDefaultLauncher

        appsRepo.liveData().observe(viewLifecycleOwner) {
            customize_app_drawer_auto_device_theme_wallpaper.isChecked = it.setThemeWallpaper
        }
        customize_app_drawer_auto_device_theme_wallpaper.setOnCheckedChangeListener { _, checked ->
            appsRepo.updateSetAutomaticDeviceWallpaper(checked)
        }
    }

    private fun setupAutomaticDeviceWallpaperSwitchText() {
        // have a title text and a subtitle text
        val text = customize_app_drawer_auto_device_theme_wallpaper.text
        val newLineIndex = text.indexOf("\n")
        if (newLineIndex == -1) {
            return
        }
        val spanBuilder = SpannableStringBuilder(customize_app_drawer_auto_device_theme_wallpaper.text)
        spanBuilder.setSpan(TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Large), 0, newLineIndex , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanBuilder.setSpan(TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Small), newLineIndex, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        customize_app_drawer_auto_device_theme_wallpaper.text = spanBuilder
    }
}
