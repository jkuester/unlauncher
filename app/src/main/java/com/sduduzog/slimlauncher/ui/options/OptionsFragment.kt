package com.sduduzog.slimlauncher.ui.options

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.navigation.Navigation
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.setKeepDeviceWallpaper
import com.jkuester.unlauncher.datasource.toggleHideStatusBar
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.dialog.AlignmentFormatDialog
import com.jkuester.unlauncher.dialog.ClockTypeDialog
import com.jkuester.unlauncher.dialog.ThemeDialog
import com.jkuester.unlauncher.dialog.TimeFormatDialog
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.OptionsFragmentBinding
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import com.sduduzog.slimlauncher.utils.isDefaultLauncher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OptionsFragment : BaseFragment() {
    @Inject
    lateinit var iActivity: ComponentActivity
    @Inject @WithFragmentLifecycle
    lateinit var corePreferencesRepo: DataRepository<CorePreferences>

    override fun getFragmentView(): ViewGroup = OptionsFragmentBinding.bind(
        requireView()
    ).optionsFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.options_fragment, container, false)

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val optionsFragment = OptionsFragmentBinding.bind(requireView())
        optionsFragment.optionsFragmentDeviceSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            launchActivity(it, intent)
        }
        optionsFragment.optionsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        optionsFragment.optionsFragmentDeviceSettings.setOnLongClickListener {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            launchActivity(it, intent)
            true
        }
        optionsFragment.optionsFragmentChangeTheme.setOnClickListener {
            ThemeDialog().showNow(childFragmentManager, null)
        }
        optionsFragment.optionsFragmentChooseTimeFormat.setOnClickListener {
            TimeFormatDialog().showNow(childFragmentManager, null)
        }
        optionsFragment.optionsFragmentChooseClockType.setOnClickListener {
            ClockTypeDialog().showNow(childFragmentManager, "CLOCK_TYPE_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseAlignment.setOnClickListener {
            AlignmentFormatDialog().showNow(childFragmentManager, "ALIGNMENT_CHOOSER")
        }
        optionsFragment.optionsFragmentToggleStatusBar.setOnClickListener {
            corePreferencesRepo.updateAsync(toggleHideStatusBar())
        }
        optionsFragment.optionsFragmentCustomizeQuickButtons.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customiseQuickButtonsFragment
            )
        )
        optionsFragment.optionsFragmentCustomizeAppDrawer.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customiseAppDrawerFragment
            )
        )
    }

    override fun onStart() {
        super.onStart()
        // setting up the switch text, since changing the default launcher re-starts the activity
        // this should able to adapt to it.
        setupAutomaticDeviceWallpaperSwitch()
    }

    private fun setupAutomaticDeviceWallpaperSwitch() {
        val appIsDefaultLauncher = isDefaultLauncher(iActivity)
        val optionsFragment = OptionsFragmentBinding.bind(requireView())
        setupDeviceWallpaperSwitchText(optionsFragment, appIsDefaultLauncher)
        optionsFragment.optionsFragmentAutoDeviceThemeWallpaper.isEnabled = appIsDefaultLauncher

        corePreferencesRepo.observe {
            // always uncheck once app isn't default launcher
            optionsFragment.optionsFragmentAutoDeviceThemeWallpaper
                .isChecked = appIsDefaultLauncher && !it.keepDeviceWallpaper
        }
        optionsFragment.optionsFragmentAutoDeviceThemeWallpaper
            .setOnCheckedChangeListener { _, checked ->
                corePreferencesRepo.updateAsync(setKeepDeviceWallpaper(!checked))
            }
    }

    /**
     * Adds a hint text underneath the default text when app is not the default launcher.
     */
    private fun setupDeviceWallpaperSwitchText(optionsFragment: OptionsFragmentBinding, appIsDefaultLauncher: Boolean) {
        val text = if (appIsDefaultLauncher) {
            getText(R.string.customize_app_drawer_fragment_auto_theme_wallpaper_text)
        } else {
            buildSwitchTextWithHint()
        }
        optionsFragment.optionsFragmentAutoDeviceThemeWallpaper.text = text
    }

    private fun buildSwitchTextWithHint(): CharSequence {
        val titleText = getText(R.string.customize_app_drawer_fragment_auto_theme_wallpaper_text)
        // have a title text and a subtitle text to indicate that adapting the
        // wallpaper can only be done when app it the default launcher
        val subTitleText = getText(
            R.string.customize_app_drawer_fragment_auto_theme_wallpaper_subtext_no_default_launcher
        )
        return createTitleAndSubtitleText(requireContext(), titleText, subTitleText)
    }
}
