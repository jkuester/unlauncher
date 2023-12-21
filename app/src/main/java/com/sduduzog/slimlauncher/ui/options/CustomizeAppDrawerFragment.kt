package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.jkuester.unlauncher.datastore.SearchBarPosition
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.customize_app_drawer_fragment
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.customize_app_drawer_fragment_search_options
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.customize_app_drawer_fragment_visible_apps
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment() {

    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customize_app_drawer_fragment_visible_apps
            .setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment))

        setupSearchFieldOptionsButton()
    }

    private fun setupSearchFieldOptionsButton() {
        customize_app_drawer_fragment_search_options.setOnClickListener(
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
                val positionText = resources.getStringArray(R.array.search_bar_position_array)[pos]
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

            customize_app_drawer_fragment_search_options.text =
                createTitleAndSubtitleText(requireContext(), title, subtitle)
        }
    }
}
