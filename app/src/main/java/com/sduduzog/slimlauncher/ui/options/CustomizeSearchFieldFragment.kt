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

    override fun getFragmentView(): ViewGroup =
        CustomizeAppDrawerFragmentSearchFieldOptionsBinding.bind(
            requireView()
        ).customizeAppDrawerFragmentSearchFieldOptions

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

        val options = CustomizeAppDrawerFragmentSearchFieldOptionsBinding.bind(
            view
        )
        options.customiseAppsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupShowSearchBarSwitch(options)
        setupSearchBarPositionOption(options)
        setupKeyboardSwitch(options)
        setupSearchAllAppsSwitch(options)
    }

    private fun setupShowSearchBarSwitch(
        options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding
    ) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        options.customizeAppDrawerFragmentShowSearchFieldSwitch
            .setOnCheckedChangeListener { _, checked ->
                prefsRepo.updateShowSearchBar(checked)
                enableSearchBarOptions(options, checked)
            }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            val checked = it.showSearchBar
            options.customizeAppDrawerFragmentShowSearchFieldSwitch.isChecked = checked
            enableSearchBarOptions(options, checked)
        }
    }

    private fun enableSearchBarOptions(
        options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding,
        enabled: Boolean
    ) {
        options.customizeAppDrawerFragmentSearchFieldPosition.isEnabled = enabled
        options.customizeAppDrawerOpenKeyboardSwitch.isEnabled = enabled
        options.customizeAppDrawerSearchAllSwitch.isEnabled = enabled
    }

    private fun setupSearchBarPositionOption(
        options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding
    ) {
        val prefRepo = unlauncherDataSource.corePreferencesRepo
        options.customizeAppDrawerFragmentSearchFieldPosition.setOnClickListener {
            val positionDialog = ChooseSearchBarPositionDialog.getSearchBarPositionChooser()
            positionDialog.showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        prefRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.customize_app_drawer_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            options.customizeAppDrawerFragmentSearchFieldPosition.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }

    private fun setupKeyboardSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        options.customizeAppDrawerOpenKeyboardSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateActivateKeyboardInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            options.customizeAppDrawerOpenKeyboardSwitch.isChecked = it.activateKeyboardInDrawer
        }
        options.customizeAppDrawerOpenKeyboardSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.customize_app_drawer_fragment_open_keyboard,
                R.string.customize_app_drawer_fragment_open_keyboard_subtitle
            )
    }

    private fun setupSearchAllAppsSwitch(
        options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding
    ) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        options.customizeAppDrawerSearchAllSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateSearchAllAppsInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            options.customizeAppDrawerSearchAllSwitch.isChecked = it.searchAllAppsInDrawer
        }
        options.customizeAppDrawerSearchAllSwitch.text =
            createTitleAndSubtitleText(
                requireContext(), R.string.customize_app_drawer_fragment_search_all,
                R.string.customize_app_drawer_fragment_search_all_subtitle
            )
    }
}
