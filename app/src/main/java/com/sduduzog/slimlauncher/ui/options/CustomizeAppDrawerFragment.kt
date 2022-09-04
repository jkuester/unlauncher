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
import com.sduduzog.slimlauncher.utils.isActivityDefaultLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.*

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment() {

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customize_app_drawer_fragment_visible_apps
            .setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment))

        setupKeyboardSwitch()
    }

    override fun onStart() {
        super.onStart()
        // setting up the switch text, since changing the default launcher re-starts the activity
        // this should able to adapt to it.
        setupAutomaticDeviceWallpaperSwitch()
    }

    private fun setupKeyboardSwitch() {
        val appsRepo = getUnlauncherDataSource().unlauncherAppsRepo
        customize_app_drawer_open_keyboard_switch.setOnCheckedChangeListener { _, checked ->
            appsRepo.updateActivateKeyboardInDrawer(checked)
        }
        appsRepo.liveData().observe(viewLifecycleOwner) {
            customize_app_drawer_open_keyboard_switch.isChecked = it.activateKeyboardInDrawer
        }
    }

    private fun setupAutomaticDeviceWallpaperSwitch() {
        val appsRepo = getUnlauncherDataSource().unlauncherAppsRepo
        val appIsDefaultLauncher = isActivityDefaultLauncher(activity)
        setupDeviceWallpaperSwitchText(appIsDefaultLauncher)
        customize_app_drawer_auto_device_theme_wallpaper.isEnabled = appIsDefaultLauncher

        appsRepo.liveData().observe(viewLifecycleOwner) {
            // always uncheck once app isn't default launcher
            customize_app_drawer_auto_device_theme_wallpaper.isChecked = appIsDefaultLauncher && it.setThemeWallpaper
        }
        customize_app_drawer_auto_device_theme_wallpaper.setOnCheckedChangeListener { _, checked ->
            appsRepo.updateSetAutomaticDeviceWallpaper(checked)
        }
    }

    /**
     * Adds a hint text underneath the default text when app is not the default launcher.
     */
    private fun setupDeviceWallpaperSwitchText(appIsDefaultLauncher: Boolean) {
        val text = if (appIsDefaultLauncher) {
            getText(R.string.customize_app_drawer_fragment_auto_theme_wallpaper_text)
        } else {
            buildSwitchTextWithHint()
        }
        customize_app_drawer_auto_device_theme_wallpaper.text = text
    }

    private fun buildSwitchTextWithHint(): CharSequence {
        val titleText = getText(R.string.customize_app_drawer_fragment_auto_theme_wallpaper_text)
        // have a title text and a subtitle text to indicate that adapting the
        // wallpaper can only be done when app it the default launcher
        val subTitleText = getText(R.string.customize_app_drawer_fragment_auto_theme_wallpaper_subtext_no_default_launcher)

        val spanBuilder = SpannableStringBuilder("$titleText\n$subTitleText")
        spanBuilder.setSpan(TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Large), 0, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanBuilder.setSpan(
            TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Small),
            titleText.length + 1,
            titleText.length + 1 + subTitleText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spanBuilder
    }
}
