package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setShowSearchBar
import com.jkuester.unlauncher.datasource.toggleActivateKeyboardInDrawer
import com.jkuester.unlauncher.datasource.toggleSearchAllAppsInDrawer
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.dialog.SearchBarPositionDialog
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerFragmentSearchFieldOptionsBinding

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

fun setupBackButton(activity: FragmentActivity) = { options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

fun setupShowSearchBarSwitch(corePrefsRepo: CorePreferencesRepository) =
    { options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding ->
        options.showSearchFieldSwitch.setOnCheckedChangeListener { _, checked ->
            corePrefsRepo.updateAsync(setShowSearchBar(checked))
        }
        corePrefsRepo.observe {
            options.showSearchFieldSwitch.isChecked = it.showSearchBar
        }
    }

fun setupSearchBarPositionOption(
    corePrefsRepo: CorePreferencesRepository,
    fragmentManager: FragmentManager,
    resources: Resources
) = { options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding ->
    options.searchFieldPosition.setOnClickListener {
        SearchBarPositionDialog().showNow(fragmentManager, null)
    }
    corePrefsRepo.observe(
        updateSearchBarPositionLayout(options, resources.getTextArray(R.array.search_bar_position_array))
    )
}

fun setupKeyboardSwitch(corePrefsRepo: CorePreferencesRepository) =
    { options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding ->
        options.openKeyboardSwitch.setOnClickListener {
            corePrefsRepo.updateAsync(toggleActivateKeyboardInDrawer())
        }
        corePrefsRepo.observe(updateKeyboardSwitchLayout(options))
    }

fun setupSearchAllAppsSwitch(corePrefsRepo: CorePreferencesRepository) =
    { options: CustomizeAppDrawerFragmentSearchFieldOptionsBinding ->
        options.searchAllSwitch.setOnClickListener {
            corePrefsRepo.updateAsync(toggleSearchAllAppsInDrawer())
        }
        corePrefsRepo.observe(updateSearchAllAppsSwitchLayout(options))
    }
