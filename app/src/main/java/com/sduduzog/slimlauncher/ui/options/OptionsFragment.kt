package com.sduduzog.slimlauncher.ui.options

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.navigation.Navigation
import com.jkuester.unlauncher.datastore.ClockType
import com.jkuester.unlauncher.datastore.MyDateFormat
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.OptionsFragmentBinding
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.ui.dialogs.ChangeThemeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseAlignmentDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseClockTypeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseDarkModeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseDateFormatDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseLead0ModifDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseSearchBarPositionDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseTimeFormatDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import com.sduduzog.slimlauncher.utils.isActivityDefaultLauncher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OptionsFragment : BaseFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource
    private lateinit var settings: SharedPreferences

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun getFragmentView(): ViewGroup = OptionsFragmentBinding.bind(
        requireView()
    ).optionsFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.options_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        settings = requireContext().getSharedPreferences(
            getString(R.string.prefs_settings),
            MODE_PRIVATE
        )
        settings.registerOnSharedPreferenceChangeListener(this)

        val prefsRepo = unlauncherDataSource.corePreferencesRepo

        val fragment = OptionsFragmentBinding.bind(requireView())
        fragment.optionsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        fragment.optionsFragmentChangeTheme.setOnClickListener {
            val dialog = ChangeThemeDialog.getThemeChooser()
            dialog.showNow(childFragmentManager, "THEME_CHOOSER")
        }
        updateThemeSubtitle()

        fragment.optionsFragmentDarkMode.setOnClickListener {
            val dialog = ChooseDarkModeDialog.getInstance()
            dialog.showNow(childFragmentManager, "DARK_MODE_CHOOSER")
        }
        updateDarkModeSubtitle()

        fragment.optionsFragmentChooseTimeFormat.setOnClickListener {
            val dialog = ChooseTimeFormatDialog.getInstance()
            dialog.showNow(childFragmentManager, "TIME_FORMAT_CHOOSER")
        }
        updateTimeFormatSubtitle()

        fragment.optionsFragmentChooseDateFormat.setOnClickListener {
            val dialog = ChooseDateFormatDialog.getInstance()
            dialog.showNow(childFragmentManager, "DATE_FORMAT_CHOOSER")
        }
        updateDateFormatSubtitle()

        fragment.optionsFragmentChooseLead0Modif.setOnClickListener {
            val dialog = ChooseLead0ModifDialog.getInstance()
            dialog.showNow(childFragmentManager, "LEAD0_MODIF_CHOOSER")
        }
        updateLead0ModifSubtitle()

        fragment.optionsFragmentChooseClockType.setOnClickListener {
            val chooseClockTypeDialog = ChooseClockTypeDialog.getInstance()
            chooseClockTypeDialog.showNow(childFragmentManager, "CLOCK_TYPE_CHOOSER")
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.clockType.number
            val title = getText(R.string.options_fragment_choose_clock_type)
            val subtitle = resources.getTextArray(R.array.clock_type_array)[position]
            fragment.optionsFragmentChooseClockType.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
            enableTimeOptions()
        }

        fragment.optionsFragmentChooseAlignment.setOnClickListener {
            val chooseAlignmentDialog = ChooseAlignmentDialog.getInstance()
            chooseAlignmentDialog.showNow(childFragmentManager, "ALIGNMENT_CHOOSER")
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.alignmentFormat.number
            val title = getText(R.string.options_fragment_choose_alignment)
            val subtitle = resources.getTextArray(R.array.alignment_format_array)[position]
            fragment.optionsFragmentChooseAlignment.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }

        fragment.optionsFragmentToggleStatusBar.setOnClickListener {
            val settings = requireContext().getSharedPreferences(
                getString(R.string.prefs_settings),
                MODE_PRIVATE
            )
            val isHidden = settings.getBoolean(
                getString(R.string.prefs_settings_key_toggle_status_bar),
                false
            )
            settings.edit {
                putBoolean(getString(R.string.prefs_settings_key_toggle_status_bar), !isHidden)
            }
        }
        fragment.optionsFragmentCustomizeApps.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeAppsFragment
            )
        )
        fragment.optionsFragmentVisibleApps.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeAppDrawerAppListFragment
            )
        )

        fragment.optionsFragmentCustomizeQuickButtons.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeQuickButtonsFragment
            )
        )

        fragment.optionsFragmentShowHeadingsSwitch
            .setOnCheckedChangeListener { _, checked ->
                prefsRepo.updateShowDrawerHeadings(checked)
            }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            fragment.optionsFragmentShowHeadingsSwitch.isChecked = it
                .showDrawerHeadings
        }
        fragment.optionsFragmentShowHeadingsSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.customize_app_drawer_fragment_show_headings,
                R.string.customize_app_drawer_fragment_show_headings_subtitle
            )

        fragment.optionsFragmentShowSearchFieldSwitch
            .setOnCheckedChangeListener { _, checked ->
                prefsRepo.updateShowSearchBar(checked)
                enableSearchBarOptions(fragment, checked)
            }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val checked = it.showSearchBar
            fragment.optionsFragmentShowSearchFieldSwitch.isChecked = checked
            enableSearchBarOptions(fragment, checked)
        }

        fragment.optionsFragmentSearchFieldPosition.setOnClickListener {
            val positionDialog = ChooseSearchBarPositionDialog.getSearchBarPositionChooser()
            positionDialog.showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.options_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            fragment.optionsFragmentSearchFieldPosition.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }

        fragment.optionsFragmentOpenKeyboardSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateActivateKeyboardInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            fragment.optionsFragmentOpenKeyboardSwitch.isChecked = it.activateKeyboardInDrawer
        }
        fragment.optionsFragmentOpenKeyboardSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.options_fragment_open_keyboard,
                R.string.options_fragment_open_keyboard_subtitle
            )

        fragment.optionsFragmentSearchAllSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateSearchAllAppsInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            fragment.optionsFragmentSearchAllSwitch.isChecked = it.searchAllAppsInDrawer
        }
        fragment.optionsFragmentSearchAllSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.options_fragment_search_all,
                R.string.options_fragment_search_all_subtitle
            )
    }

    private fun updateThemeSubtitle() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val position = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)
        val title = getText(R.string.options_fragment_change_theme)
        val subtitle = resources.getTextArray(R.array.themes_array)[position]
        fragment.optionsFragmentChangeTheme.text =
            createTitleAndSubtitleText(requireContext(), title, subtitle)
    }

    private fun updateDarkModeSubtitle() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val position = settings.getInt(getString(R.string.prefs_settings_key_dark_mode), 0)
        val title = getText(R.string.options_fragment_dark_mode)
        val subtitle = resources.getTextArray(R.array.dark_mode_array)[position]
        fragment.optionsFragmentDarkMode.text =
            createTitleAndSubtitleText(requireContext(), title, subtitle)
    }

    private fun updateTimeFormatSubtitle() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val position = settings.getInt(getString(R.string.prefs_settings_key_time_format), 0)
        val title = getText(R.string.options_fragment_choose_time_format)
        val subtitle = resources.getTextArray(R.array.time_format_array)[position]
        fragment.optionsFragmentChooseTimeFormat.text =
            createTitleAndSubtitleText(requireContext(), title, subtitle)
    }

    private fun updateDateFormatSubtitle() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val position = settings.getInt(getString(R.string.prefs_settings_key_date_format), 0)
        val title = getText(R.string.options_fragment_choose_date_format)
        val subtitle = resources.getTextArray(R.array.date_format_array)[position]
        fragment.optionsFragmentChooseDateFormat.text =
            createTitleAndSubtitleText(requireContext(), title, subtitle)
    }

    private fun updateLead0ModifSubtitle() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val position = settings.getInt(getString(R.string.prefs_settings_key_lead0_modif), 0)
        val title = getText(R.string.options_fragment_choose_lead0_modif)
        val subtitle = resources.getTextArray(R.array.lead0_modif_array)[position]
        fragment.optionsFragmentChooseLead0Modif.text =
            createTitleAndSubtitleText(requireContext(), title, subtitle)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (s.equals(getString(R.string.prefs_settings_key_theme), true)) {
            updateThemeSubtitle()
        }
        if (s.equals(getString(R.string.prefs_settings_key_time_format), true)) {
            updateTimeFormatSubtitle()
        }
        if (s.equals(getString(R.string.prefs_settings_key_date_format), true)) {
            updateDateFormatSubtitle()
            enableTimeOptions()
        }
        if (s.equals(getString(R.string.prefs_settings_key_lead0_modif), true)) {
            updateLead0ModifSubtitle()
        }
    }

    private fun enableSearchBarOptions(fragment: OptionsFragmentBinding, enabled: Boolean) {
        fragment.optionsFragmentSearchFieldPosition.isEnabled = enabled
        fragment.optionsFragmentOpenKeyboardSwitch.isEnabled = enabled
        fragment.optionsFragmentSearchAllSwitch.isEnabled = enabled

        val title = R.string.customize_app_drawer_fragment_show_search_bar
        val sub = if (enabled) {
            R.string.customize_app_drawer_search_bar_enabled
        } else {
            R.string.customize_app_drawer_search_bar_disabled
        }
        fragment.optionsFragmentShowSearchFieldSwitch.text =
            createTitleAndSubtitleText(requireContext(), getText(title), getText(sub))
    }

    private fun enableTimeOptions() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        val clockType = prefsRepo.get().clockType.number
        val isDigital = (clockType == ClockType.digital.number)
        val haveClock = (clockType != ClockType.none.number)

        val position = settings.getInt(getString(R.string.prefs_settings_key_date_format), 0)
        val haveDate = (position != MyDateFormat.date_none.number)

        fragment.optionsFragmentChooseTimeFormat.isEnabled = haveClock && isDigital
        fragment.optionsFragmentChooseDateFormat.isEnabled = haveClock
        fragment.optionsFragmentChooseLead0Modif.isEnabled = haveClock && (haveDate || isDigital)
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
        val optionsFragment = OptionsFragmentBinding.bind(requireView())
        setupDeviceWallpaperSwitchText(optionsFragment, appIsDefaultLauncher)
        optionsFragment.optionsFragmentAutoDeviceThemeWallpaper.isEnabled = appIsDefaultLauncher

        prefsRepo.liveData().observe(viewLifecycleOwner) {
            // always uncheck once app isn't default launcher
            val enabled = appIsDefaultLauncher && !it.keepDeviceWallpaper
            optionsFragment.optionsFragmentAutoDeviceThemeWallpaper.isChecked = enabled
            setupDeviceWallpaperSwitchText(optionsFragment, appIsDefaultLauncher)
        }
        optionsFragment.optionsFragmentAutoDeviceThemeWallpaper
            .setOnCheckedChangeListener { _, checked ->
                prefsRepo.updateKeepDeviceWallpaper(!checked)
            }
    }

    /**
     * Adds a hint text underneath the default text when app is not the default launcher.
     */
    private fun setupDeviceWallpaperSwitchText(
        optionsFragment: OptionsFragmentBinding,
        appIsDefaultLauncher: Boolean
    ) {
        val titleText = getText(R.string.customize_app_drawer_fragment_wallpaper_text)
        val subTitleText = getText(
            if (appIsDefaultLauncher) {
                val prefsRepo = unlauncherDataSource.corePreferencesRepo
                if (prefsRepo.get().keepDeviceWallpaper) {
                    R.string.customize_app_drawer_fragment_wallpaper_disabled
                } else {
                    R.string.customize_app_drawer_fragment_wallpaper_enabled
                }
            } else {
                R.string.customize_app_drawer_fragment_wallpaper_not_default_launcher
            }
        )
        optionsFragment.optionsFragmentAutoDeviceThemeWallpaper.text =
            createTitleAndSubtitleText(requireContext(), titleText, subTitleText)
    }
}
