package com.sduduzog.slimlauncher.ui.options

import android.content.Context.MODE_PRIVATE
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
class OptionsFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

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
        val optionsFragment = OptionsFragmentBinding.bind(requireView())
        optionsFragment.optionsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        optionsFragment.optionsFragmentChangeTheme.setOnClickListener {
            val changeThemeDialog = ChangeThemeDialog.getThemeChooser()
            changeThemeDialog.showNow(childFragmentManager, "THEME_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseTimeFormat.setOnClickListener {
            val chooseTimeFormatDialog = ChooseTimeFormatDialog.getInstance()
            chooseTimeFormatDialog.showNow(childFragmentManager, "TIME_FORMAT_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseClockType.setOnClickListener {
            val chooseClockTypeDialog = ChooseClockTypeDialog.getInstance()
            chooseClockTypeDialog.showNow(childFragmentManager, "CLOCK_TYPE_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseAlignment.setOnClickListener {
            val chooseAlignmentDialog = ChooseAlignmentDialog.getInstance()
            chooseAlignmentDialog.showNow(childFragmentManager, "ALIGNMENT_CHOOSER")
        }
        optionsFragment.optionsFragmentToggleStatusBar.setOnClickListener {
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
        optionsFragment.optionsFragmentCustomizeApps.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeAppsFragment
            )
        )
        optionsFragment.optionsFragmentVisibleApps.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeAppDrawerAppListFragment
            )
        )

        optionsFragment.optionsFragmentCustomizeQuickButtons.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeQuickButtonsFragment
            )
        )
        setupHeadingSwitch(optionsFragment)
        setupShowSearchBarSwitch(optionsFragment)
        setupSearchBarPositionOption(optionsFragment)
        setupKeyboardSwitch(optionsFragment)
        setupSearchAllAppsSwitch(optionsFragment)
    }

    private fun setupHeadingSwitch(optionsFragment: OptionsFragmentBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        optionsFragment.optionsFragmentShowHeadingsSwitch
            .setOnCheckedChangeListener { _, checked ->
                prefsRepo.updateShowDrawerHeadings(checked)
            }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            optionsFragment.optionsFragmentShowHeadingsSwitch.isChecked = it
                .showDrawerHeadings
        }
        optionsFragment.optionsFragmentShowHeadingsSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.customize_app_drawer_fragment_show_headings,
                R.string.customize_app_drawer_fragment_show_headings_subtitle
            )
    }

    private fun setupShowSearchBarSwitch(options: OptionsFragmentBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        options.optionsFragmentShowSearchFieldSwitch
            .setOnCheckedChangeListener { _, checked ->
                prefsRepo.updateShowSearchBar(checked)
                enableSearchBarOptions(options, checked)
            }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val checked = it.showSearchBar
            options.optionsFragmentShowSearchFieldSwitch.isChecked = checked
            enableSearchBarOptions(options, checked)
        }
    }

    private fun enableSearchBarOptions(options: OptionsFragmentBinding, enabled: Boolean) {
        options.optionsFragmentSearchFieldPosition.isEnabled = enabled
        options.optionsFragmentOpenKeyboardSwitch.isEnabled = enabled
        options.optionsFragmentSearchAllSwitch.isEnabled = enabled
    }

    private fun setupSearchBarPositionOption(options: OptionsFragmentBinding) {
        val prefRepo = unlauncherDataSource.corePreferencesRepo
        options.optionsFragmentSearchFieldPosition.setOnClickListener {
            val positionDialog = ChooseSearchBarPositionDialog.getSearchBarPositionChooser()
            positionDialog.showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        prefRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.options_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            options.optionsFragmentSearchFieldPosition.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }

    private fun setupKeyboardSwitch(options: OptionsFragmentBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        options.optionsFragmentOpenKeyboardSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateActivateKeyboardInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            options.optionsFragmentOpenKeyboardSwitch.isChecked = it.activateKeyboardInDrawer
        }
        options.optionsFragmentOpenKeyboardSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.options_fragment_open_keyboard,
                R.string.options_fragment_open_keyboard_subtitle
            )
    }

    private fun setupSearchAllAppsSwitch(options: OptionsFragmentBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        options.optionsFragmentSearchAllSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateSearchAllAppsInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            options.optionsFragmentSearchAllSwitch.isChecked = it.searchAllAppsInDrawer
        }
        options.optionsFragmentSearchAllSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.options_fragment_search_all,
                R.string.options_fragment_search_all_subtitle
            )
    }
}
