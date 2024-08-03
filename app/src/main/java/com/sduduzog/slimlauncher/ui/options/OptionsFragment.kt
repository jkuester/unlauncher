package com.sduduzog.slimlauncher.ui.options

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.OptionsFragmentBinding
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.ui.dialogs.ChangeThemeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseAlignmentDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseClockTypeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseSearchBarPositionDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseTimeFormatDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
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
            val changeThemeDialog = ChangeThemeDialog.getThemeChooser()
            changeThemeDialog.showNow(childFragmentManager, "THEME_CHOOSER")
        }
        updateThemeSubtitle()

        fragment.optionsFragmentChooseTimeFormat.setOnClickListener {
            val chooseTimeFormatDialog = ChooseTimeFormatDialog.getInstance()
            chooseTimeFormatDialog.showNow(childFragmentManager, "TIME_FORMAT_CHOOSER")
        }
        updateTimeFormatSubtitle()

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

    private fun updateTimeFormatSubtitle() {
        val fragment = OptionsFragmentBinding.bind(requireView())
        val position = settings.getInt(getString(R.string.prefs_settings_key_time_format), 0)
        val title = getText(R.string.options_fragment_choose_time_format)
        val subtitle = resources.getTextArray(R.array.time_format_array)[position]
        fragment.optionsFragmentChooseTimeFormat.text =
            createTitleAndSubtitleText(requireContext(), title, subtitle)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (s.equals(getString(R.string.prefs_settings_key_theme), true)) {
            updateThemeSubtitle()
        }
        if (s.equals(getString(R.string.prefs_settings_key_time_format), true)) {
            updateTimeFormatSubtitle()
        }
    }

    private fun enableSearchBarOptions(fragment: OptionsFragmentBinding, enabled: Boolean) {
        fragment.optionsFragmentSearchFieldPosition.isEnabled = enabled
        fragment.optionsFragmentOpenKeyboardSwitch.isEnabled = enabled
        fragment.optionsFragmentSearchAllSwitch.isEnabled = enabled
    }
}
