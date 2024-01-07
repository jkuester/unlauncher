package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.jkuester.unlauncher.datastore.SearchBarPosition
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerFragmentBinding
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment() {

    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = CustomizeAppDrawerFragmentBinding.bind(requireView()).customizeAppDrawerFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customiseAppDrawerFragment = CustomizeAppDrawerFragmentBinding.bind(view)
        customiseAppDrawerFragment.customizeAppDrawerFragmentVisibleApps
            .setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment))

        customiseAppDrawerFragment.customizeAppDrawerFragmentBack.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        setupSearchFieldOptionsButton(customiseAppDrawerFragment)
        setupHeadingSwitch(customiseAppDrawerFragment)
    }

    private fun setupSearchFieldOptionsButton(customiseAppDrawerFragment: CustomizeAppDrawerFragmentBinding) {
        customiseAppDrawerFragment.customizeAppDrawerFragmentSearchOptions.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customizeSearchFieldFragment)
        )
        val preferencesRepository = unlauncherDataSource.corePreferencesRepo
        val title = getText(R.string.customize_app_drawer_fragment_search_field_options)
        preferencesRepository.liveData().observe(viewLifecycleOwner) {
            val subtitle = if (!it.hasShowSearchBar() || it.showSearchBar) {
                val pos =
                    if (it.searchBarPosition == SearchBarPosition.UNRECOGNIZED) {
                        SearchBarPosition.top.number
                    } else {
                        it.searchBarPosition.number
                    }
                val positionText = resources.getStringArray(R.array.search_bar_position_array)[pos].lowercase()
                val keyboardShownText =
                    if (it.activateKeyboardInDrawer) getText(R.string.shown) else getText(R.string.hidden)
                getString(
                    R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_shown,
                    positionText,
                    keyboardShownText
                )
            } else {
                getText(R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_hidden)
            }

            customiseAppDrawerFragment.customizeAppDrawerFragmentSearchOptions.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }

    private fun setupHeadingSwitch(customiseAppDrawerFragment: CustomizeAppDrawerFragmentBinding) {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customiseAppDrawerFragment.customizeAppDrawerFragmentShowHeadingsSwitch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateShowDrawerHeadings(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            customiseAppDrawerFragment.customizeAppDrawerFragmentShowHeadingsSwitch.isChecked = it.showDrawerHeadings
        }
        customiseAppDrawerFragment.customizeAppDrawerFragmentShowHeadingsSwitch.text =
            createTitleAndSubtitleText(
                    requireContext(), R.string.customize_app_drawer_fragment_show_headings,
                    R.string.customize_app_drawer_fragment_show_headings_subtitle
            )
    }
}
