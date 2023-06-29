package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.jkuester.unlauncher.datastore.CorePreferences
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.datasource.coreprefs.CorePreferencesRepository
import com.sduduzog.slimlauncher.ui.dialogs.ChooseSearchBarPositionDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.createTitleAndSubtitleText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.*
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

        setupShowSearchBarSwitch()
        setupSearchBarPositionOption()
        setupKeyboardSwitch()
    }

    private fun setupShowSearchBarSwitch() {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_fragment_search_bar.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateShowSearchBar(checked)
            enableSearchBarOptions(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            // when upgrading from an older version the property showSearchBar is null
            // we therefore set default state of the switch to true
            // this has the reason that protobuf 3 does not allow default values,
            // see https://stackoverflow.com/a/62435235
            // hence making the showSearchBar attribute optional and therefore allow to be null
            // this is a logical implication: checked = hasShowSearchBar -> showSearchBar
            val checked = !it.hasShowSearchBar() || it.showSearchBar
            customize_app_drawer_fragment_search_bar.isChecked = checked
            enableSearchBarOptions(it.showSearchBar)
        }
    }

    private fun enableSearchBarOptions(enabled: Boolean) {
        customize_app_drawer_fragment_search_bar_position.isEnabled = enabled
        customize_app_drawer_open_keyboard_switch.isEnabled = enabled
    }

    private fun setupSearchBarPositionOption() {
        val prefRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_fragment_search_bar_position.setOnClickListener {
            val positionDialog = ChooseSearchBarPositionDialog.getSearchBarPositionChooser()
            positionDialog.showNow(childFragmentManager, "POSITION_CHOOSER")
        }
        prefRepo.liveData().observe(viewLifecycleOwner) {
            val position = it.searchBarPosition.number
            val title = getText(R.string.customize_app_drawer_fragment_search_bar_position)
            val subtitle = resources.getTextArray(R.array.search_bar_position_array)[position]
            customize_app_drawer_fragment_search_bar_position.text =
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
    }
}
