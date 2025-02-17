package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import android.view.View.OnClickListener
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setShowSearchBar
import com.jkuester.unlauncher.datasource.toggleActivateKeyboardInDrawer
import com.jkuester.unlauncher.datasource.toggleSearchAllAppsInDrawer
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.dialog.SearchBarPositionDialog
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerSearchFieldOptionsBinding

fun setupBackButton(activity: ComponentActivity) = { options: CustomizeAppDrawerSearchFieldOptionsBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

fun setupShowSearchBarSwitch(corePrefsRepo: CorePreferencesRepository) =
    { options: CustomizeAppDrawerSearchFieldOptionsBinding ->
        options.showSearchFieldSwitch.setOnCheckedChangeListener { _, checked ->
            corePrefsRepo.updateAsync(setShowSearchBar(checked))
        }
        corePrefsRepo.observe {
            options.showSearchFieldSwitch.isChecked = it.showSearchBar
        }
    }

private fun searchFieldPositionListener(fragmentManager: FragmentManager) = OnClickListener {
    SearchBarPositionDialog().showNow(fragmentManager, null)
}

private fun updateSearchBarPositionLayout(
    options: CustomizeAppDrawerSearchFieldOptionsBinding,
    optionNames: Array<CharSequence>
): (CorePreferences) -> Unit = {
    options.apply {
        searchFieldPositionTitle.isEnabled = it.showSearchBar
        searchFieldPositionSubtitle.isEnabled = it.showSearchBar
        searchFieldPositionSubtitle.text = optionNames[it.searchBarPosition.number]
    }
}

fun setupSearchBarPositionOption(
    corePrefsRepo: CorePreferencesRepository,
    fragmentManager: FragmentManager,
    resources: Resources
) = { options: CustomizeAppDrawerSearchFieldOptionsBinding ->
    searchFieldPositionListener(fragmentManager)
        .also(options.searchFieldPositionTitle::setOnClickListener)
        .also(options.searchFieldPositionSubtitle::setOnClickListener)
    corePrefsRepo.observe(
        updateSearchBarPositionLayout(options, resources.getTextArray(R.array.search_bar_position_array))
    )
}

private fun openKeyboardSwitchListener(corePrefsRepo: CorePreferencesRepository) = OnClickListener {
    corePrefsRepo.updateAsync(toggleActivateKeyboardInDrawer())
}

private fun updateKeyboardSwitchLayout(
    options: CustomizeAppDrawerSearchFieldOptionsBinding
): (CorePreferences) -> Unit = {
    options.apply {
        openKeyboardSwitchTitle.isEnabled = it.showSearchBar
        openKeyboardSwitchSubtitle.isEnabled = it.showSearchBar
        openKeyboardSwitchToggle.isEnabled = it.showSearchBar
        openKeyboardSwitchToggle.isChecked = it.activateKeyboardInDrawer
    }
}

fun setupKeyboardSwitch(corePrefsRepo: CorePreferencesRepository) =
    { options: CustomizeAppDrawerSearchFieldOptionsBinding ->
        openKeyboardSwitchListener(corePrefsRepo)
            .also(options.openKeyboardSwitchTitle::setOnClickListener)
            .also(options.openKeyboardSwitchSubtitle::setOnClickListener)
            .also(options.openKeyboardSwitchToggle::setOnClickListener)
        corePrefsRepo.observe(updateKeyboardSwitchLayout(options))
    }

private fun searchAllAppsListener(corePrefsRepo: CorePreferencesRepository) = OnClickListener {
    corePrefsRepo.updateAsync(toggleSearchAllAppsInDrawer())
}

private fun updateSearchAllAppsSwitchLayout(
    options: CustomizeAppDrawerSearchFieldOptionsBinding
): (CorePreferences) -> Unit = {
    options.apply {
        searchAllSwitchTitle.isEnabled = it.showSearchBar
        searchAllSwitchSubtitle.isEnabled = it.showSearchBar
        searchAllSwitchToggle.isEnabled = it.showSearchBar
        searchAllSwitchToggle.isChecked = it.searchAllAppsInDrawer
    }
}

fun setupSearchAllAppsSwitch(corePrefsRepo: CorePreferencesRepository) =
    { options: CustomizeAppDrawerSearchFieldOptionsBinding ->
        searchAllAppsListener(corePrefsRepo)
            .also(options.searchAllSwitchTitle::setOnClickListener)
            .also(options.searchAllSwitchSubtitle::setOnClickListener)
            .also(options.searchAllSwitchToggle::setOnClickListener)
        corePrefsRepo.observe(updateSearchAllAppsSwitchLayout(options))
    }
