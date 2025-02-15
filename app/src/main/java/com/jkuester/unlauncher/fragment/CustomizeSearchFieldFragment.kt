package com.jkuester.unlauncher.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setShowSearchBar
import com.jkuester.unlauncher.datasource.toggleActivateKeyboardInDrawer
import com.jkuester.unlauncher.datasource.toggleSearchAllAppsInDrawer
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.dialog.SearchBarPositionDialog
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerFragmentSearchFieldOptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private fun setupBackButton(activity: FragmentActivity) =
    { options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding ->
        options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
    }

private fun updateSearchBarPositionLayout(
    options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding,
    optionNames: Array<CharSequence>
): (CorePreferences) -> Unit = {
    options.apply {
        searchFieldPosition.isEnabled = it.showSearchBar
        searchFieldPositionTitle.isEnabled = it.showSearchBar
        searchFieldPositionSubtitle.isEnabled = it.showSearchBar
        searchFieldPositionSubtitle.text = optionNames[it.searchBarPosition.number]
    }
}

private fun updateKeyboardSwitchLayout(
    options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding
): (CorePreferences) -> Unit = {
    options.apply {
        openKeyboardSwitch.isEnabled = it.showSearchBar
        openKeyboardSwitchTitle.isEnabled = it.showSearchBar
        openKeyboardSwitchSubtitle.isEnabled = it.showSearchBar
        openKeyboardSwitchToggle.isEnabled = it.showSearchBar
        openKeyboardSwitchToggle.isChecked = it.activateKeyboardInDrawer
    }
}

private fun updateSearchAllAppsSwitchLayout(
    options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding
): (CorePreferences) -> Unit = {
    options.apply {
        searchAllSwitch.isEnabled = it.showSearchBar
        searchAllSwitchTitle.isEnabled = it.showSearchBar
        searchAllSwitchSubtitle.isEnabled = it.showSearchBar
        searchAllSwitchToggle.isEnabled = it.showSearchBar
        searchAllSwitchToggle.isChecked = it.searchAllAppsInDrawer
    }
}

@AndroidEntryPoint
class CustomizeSearchFieldFragment : Fragment() {
    @Inject
    lateinit var corePrefsRepo: CorePreferencesRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_app_drawer_fragment_search_field_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomizeAppDrawerFragmentSearchFieldOptionsBinding
            .bind(view)
            .also(setupBackButton(requireActivity()))
            .also(this::setupShowSearchBarSwitch)
            .also(this::setupSearchBarPositionOption)
            .also(this::setupKeyboardSwitch)
            .also(this::setupSearchAllAppsSwitch)
    }

    private fun setupShowSearchBarSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.showSearchFieldSwitch.setOnCheckedChangeListener { _, checked ->
            corePrefsRepo.updateAsync(setShowSearchBar(checked))
        }
        corePrefsRepo.observe(viewLifecycleOwner) {
            options.showSearchFieldSwitch.isChecked = it.showSearchBar
        }
    }

    private fun setupSearchBarPositionOption(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.searchFieldPosition.setOnClickListener {
            SearchBarPositionDialog().showNow(childFragmentManager, null)
        }
        corePrefsRepo.observe(
            viewLifecycleOwner,
            updateSearchBarPositionLayout(options, resources.getTextArray(R.array.search_bar_position_array))
        )
    }

    private fun setupKeyboardSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.openKeyboardSwitch.setOnClickListener {
            corePrefsRepo.updateAsync(toggleActivateKeyboardInDrawer())
        }
        corePrefsRepo.observe(viewLifecycleOwner, updateKeyboardSwitchLayout(options))
    }

    private fun setupSearchAllAppsSwitch(options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding) {
        options.searchAllSwitch.setOnClickListener {
            corePrefsRepo.updateAsync(toggleSearchAllAppsInDrawer())
        }
        corePrefsRepo.observe(viewLifecycleOwner, updateSearchAllAppsSwitchLayout(options))
    }
}
