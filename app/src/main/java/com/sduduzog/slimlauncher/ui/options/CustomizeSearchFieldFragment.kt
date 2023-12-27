package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.ui.dialogs.ChooseSearchBarPositionDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment_search_field_options.customize_app_drawer_fragment_search_field_options

import kotlinx.android.synthetic.main.customize_app_drawer_fragment_search_field_options.customize_app_drawer_fragment_search_field_position
import kotlinx.android.synthetic.main.customize_app_drawer_fragment_search_field_options.customize_app_drawer_fragment_show_search_field_switch
import kotlinx.android.synthetic.main.customize_app_drawer_fragment_search_field_options.customize_app_drawer_open_keyboard_switch
import kotlinx.android.synthetic.main.customize_app_drawer_fragment_search_field_options.customize_app_drawer_search_all_switch

import javax.inject.Inject

@AndroidEntryPoint
class CustomizeSearchFieldFragment : BaseFragment() {

    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment_search_field_options

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.customize_app_drawer_fragment_search_field_options,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupShowSearchBarSwitch()
        setupSearchBarPositionOption()
        setupKeyboardSwitch()
        setupSearchAllAppsSwitch()
    }

    private fun setupShowSearchBarSwitch() {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_fragment_show_search_field_switch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.showSearchField = checked
            enableSearchBarOptions(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val checked = prefsRepo.showSearchField
            customize_app_drawer_fragment_show_search_field_switch.isChecked = checked
            enableSearchBarOptions(checked)
        }
    }

    private fun enableSearchBarOptions(enabled: Boolean) {
        customize_app_drawer_fragment_search_field_position.isEnabled = enabled
        customize_app_drawer_open_keyboard_switch.isEnabled = enabled
    }

    private fun setupSearchBarPositionOption() {
        val prefRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_fragment_search_field_position.setOnClickListener {
            val positionDialog = ChooseSearchBarPositionDialog.getSearchBarPositionChooser()
            positionDialog.showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        prefRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.customize_app_drawer_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            customize_app_drawer_fragment_search_field_position.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }

    private fun setupKeyboardSwitch() {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_open_keyboard_switch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateActivateKeyboardInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            customize_app_drawer_open_keyboard_switch.isChecked = it.activateKeyboardInDrawer
        }
        customize_app_drawer_open_keyboard_switch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.customize_app_drawer_fragment_open_keyboard,
                R.string.customize_app_drawer_fragment_open_keyboard_subtitle
            )
    }

    private fun setupSearchAllAppsSwitch() {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_search_all_switch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateSearchAllAppsInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            customize_app_drawer_search_all_switch.isChecked = it.searchAllAppsInDrawer
        }
        customize_app_drawer_search_all_switch.text =
                createTitleAndSubtitleText(
                        requireContext(), R.string.customize_app_drawer_fragment_search_all,
                        R.string.customize_app_drawer_fragment_search_all_subtitle
                )
    }
}