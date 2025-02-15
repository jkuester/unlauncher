package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setActivateKeyboardInDrawer
import com.jkuester.unlauncher.datasource.setSearchAllAppsInDrawer
import com.jkuester.unlauncher.datasource.setShowSearchBar
import com.jkuester.unlauncher.dialog.SearchBarPositionDialog
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerFragmentSearchFieldOptionsBinding
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeSearchFieldFragment : BaseFragment() {

    @Inject
    lateinit var corePreferencesRepo: CorePreferencesRepository

    override fun getFragmentView(): ViewGroup = CustomizeAppDrawerFragmentSearchFieldOptionsBinding.bind(
        requireView()
    ).customizeAppDrawerFragmentSearchFieldOptions

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(
            R.layout.customize_app_drawer_fragment_search_field_options,
            container,
            false
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = CustomizeAppDrawerFragmentSearchFieldOptionsBinding.bind(
            view
        )
        options.headerBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupShowSearchBarSwitch(options)
        setupSearchBarPositionOption(options)
        setupKeyboardSwitch(options)
        setupSearchAllAppsSwitch(options)
    }

    private fun setupShowSearchBarSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.showSearchFieldSwitch
            .setOnCheckedChangeListener { _, checked ->
                corePreferencesRepo.updateAsync(setShowSearchBar(checked))
                enableSearchBarOptions(options, checked)
            }
        corePreferencesRepo.observe(viewLifecycleOwner) {
            val checked = it.showSearchBar
            options.showSearchFieldSwitch.isChecked = checked
            enableSearchBarOptions(options, checked)
        }
    }

    private fun enableSearchBarOptions(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding, enabled: Boolean) {
        options.searchFieldPosition.isEnabled = enabled
        options.openKeyboardSwitch.isEnabled = enabled
        options.searchAllSwitch.isEnabled = enabled
    }

    private fun setupSearchBarPositionOption(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.searchFieldPosition.setOnClickListener {
            SearchBarPositionDialog().showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        corePreferencesRepo.observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.customize_app_drawer_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            options.searchFieldPosition.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }

    private fun setupKeyboardSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.openKeyboardSwitch.setOnCheckedChangeListener { _, checked ->
            corePreferencesRepo.updateAsync(setActivateKeyboardInDrawer(checked))
        }
        corePreferencesRepo.observe(viewLifecycleOwner) {
            options.openKeyboardSwitch.isChecked = it.activateKeyboardInDrawer
        }
        options.openKeyboardSwitch.text =
            createTitleAndSubtitleText(
                requireContext(),
                R.string.customize_app_drawer_fragment_open_keyboard,
                R.string.customize_app_drawer_fragment_open_keyboard_subtitle
            )
    }

    private fun setupSearchAllAppsSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.searchAllSwitch.setOnCheckedChangeListener { _, checked ->
            corePreferencesRepo.updateAsync(setSearchAllAppsInDrawer(checked))
        }
        corePreferencesRepo.observe(viewLifecycleOwner) {
            options.searchAllSwitch.isChecked = it.searchAllAppsInDrawer
        }
        options.searchAllSwitch.text =
            createTitleAndSubtitleText(
                requireContext(),
                R.string.customize_app_drawer_fragment_search_all,
                R.string.customize_app_drawer_fragment_search_all_subtitle
            )
    }
}
