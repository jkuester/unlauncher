package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerFragmentSearchFieldOptionsBinding
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.ui.dialogs.ChooseSearchBarPositionDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeSearchFieldFragment : BaseFragment() {

    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = CustomizeAppDrawerFragmentSearchFieldOptionsBinding.bind(requireView()).customizeAppDrawerFragmentSearchFieldOptions

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

        val customizeAppDrawerFragmentSearchFieldOptions = CustomizeAppDrawerFragmentSearchFieldOptionsBinding.bind(view)
        customizeAppDrawerFragmentSearchFieldOptions.customiseAppsFragmentBack.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupShowSearchBarSwitch(customizeAppDrawerFragmentSearchFieldOptions)
        setupSearchBarPositionOption(customizeAppDrawerFragmentSearchFieldOptions)
        setupKeyboardSwitch(customizeAppDrawerFragmentSearchFieldOptions)
        setupSearchAllAppsSwitch(customizeAppDrawerFragmentSearchFieldOptions)
    }

    private fun setupShowSearchBarSwitch(customizeAppDrawerFragmentSearchFieldOptions: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerFragmentShowSearchFieldSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateShowSearchBar(checked)
            enableSearchBarOptions(customizeAppDrawerFragmentSearchFieldOptions, checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val checked = it.showSearchBar
            customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerFragmentShowSearchFieldSwitch.isChecked = checked
            enableSearchBarOptions(customizeAppDrawerFragmentSearchFieldOptions, checked)
        }
    }

    private fun enableSearchBarOptions(customizeAppDrawerFragmentSearchFieldOptions: CustomizeAppDrawerFragmentSearchFieldOptionsBinding, enabled: Boolean) {
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerFragmentSearchFieldPosition.isEnabled = enabled
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerOpenKeyboardSwitch.isEnabled = enabled
    }

    private fun setupSearchBarPositionOption(customizeAppDrawerFragmentSearchFieldOptions: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        val prefRepo = unlauncherDataSource.corePreferencesRepo
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerFragmentSearchFieldPosition.setOnClickListener {
            val positionDialog = ChooseSearchBarPositionDialog.getSearchBarPositionChooser()
            positionDialog.showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        prefRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.customize_app_drawer_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerFragmentSearchFieldPosition.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }

    private fun setupKeyboardSwitch(customizeAppDrawerFragmentSearchFieldOptions: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerOpenKeyboardSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateActivateKeyboardInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerOpenKeyboardSwitch.isChecked = it.activateKeyboardInDrawer
        }
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerOpenKeyboardSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.customize_app_drawer_fragment_open_keyboard,
                R.string.customize_app_drawer_fragment_open_keyboard_subtitle
            )
    }

    private fun setupSearchAllAppsSwitch(customizeAppDrawerFragmentSearchFieldOptions: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerSearchAllSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateSearchAllAppsInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerSearchAllSwitch.isChecked = it.searchAllAppsInDrawer
        }
        customizeAppDrawerFragmentSearchFieldOptions.customizeAppDrawerSearchAllSwitch.text =
                createTitleAndSubtitleText(
                        requireContext(), R.string.customize_app_drawer_fragment_search_all,
                        R.string.customize_app_drawer_fragment_search_all_subtitle
                )
    }
}