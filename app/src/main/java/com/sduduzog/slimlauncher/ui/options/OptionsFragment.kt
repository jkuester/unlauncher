package com.sduduzog.slimlauncher.ui.options

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.ui.dialogs.ChangeThemeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseTimeFormatDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.isActivityDefaultLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.options_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class OptionsFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = options_fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.options_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        options_fragment_device_settings.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            launchActivity(it, intent)
        }
        options_fragment_device_settings.setOnLongClickListener {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            launchActivity(it, intent)
            true
        }
        options_fragment_change_theme.setOnClickListener {
            val changeThemeDialog = ChangeThemeDialog.getThemeChooser()
            changeThemeDialog.showNow(childFragmentManager, "THEME_CHOOSER")
        }
        options_fragment_choose_time_format.setOnClickListener {
            val chooseTimeFormatDialog = ChooseTimeFormatDialog.getInstance()
            chooseTimeFormatDialog.showNow(childFragmentManager, "TIME_FORMAT_CHOOSER")
        }
        options_fragment_toggle_status_bar.setOnClickListener {
            val settings = requireContext().getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)
            val isHidden = settings.getBoolean(getString(R.string.prefs_settings_key_toggle_status_bar), false)
            settings.edit {
                putBoolean(getString(R.string.prefs_settings_key_toggle_status_bar), !isHidden)
            }
        }
        options_fragment_customise_apps.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_optionsFragment_to_customiseAppsFragment))
        options_fragment_customize_quick_buttons.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_optionsFragment_to_customiseQuickButtonsFragment))
        options_fragment_customize_app_drawer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_optionsFragment_to_customiseAppDrawerFragment))
    }

    override fun onStart() {
        super.onStart()
        // setting up the switch text, since changing the default launcher re-starts the activity
        // this should able to adapt to it.
        setupAutomaticDeviceWallpaperSwitch()
    }

    private fun setupAutomaticDeviceWallpaperSwitch() {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        val appIsDefaultLauncher = isActivityDefaultLauncher(activity)
        setupDeviceWallpaperSwitchText(appIsDefaultLauncher)
        options_fragment_auto_device_theme_wallpaper.isEnabled = appIsDefaultLauncher

        prefsRepo.liveData().observe(viewLifecycleOwner) {
            // always uncheck once app isn't default launcher
            options_fragment_auto_device_theme_wallpaper.isChecked = appIsDefaultLauncher && !it.keepDeviceWallpaper
        }
        options_fragment_auto_device_theme_wallpaper.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateKeepDeviceWallpaper(!checked)
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
        options_fragment_auto_device_theme_wallpaper.text = text
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