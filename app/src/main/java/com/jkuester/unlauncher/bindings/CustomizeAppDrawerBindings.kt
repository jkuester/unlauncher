package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import android.view.View.OnClickListener
import androidx.activity.ComponentActivity
import androidx.navigation.Navigation
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.toggleShowDrawerHeadings
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerBinding

fun setupCustomizeAppDrawerBackButton(activity: ComponentActivity) = { options: CustomizeAppDrawerBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

private fun showHeadingSwitchListener(corePrefsRepo: DataRepository<CorePreferences>) = OnClickListener {
    corePrefsRepo.updateAsync(toggleShowDrawerHeadings())
}

private fun updateShowHeadingSwitchLayout(options: CustomizeAppDrawerBinding): (CorePreferences) -> Unit = {
    options.apply { showHeadingsSwitchToggle.isChecked = it.showDrawerHeadings }
}

fun setupShowHeadingSwitch(corePrefsRepo: DataRepository<CorePreferences>) = { options: CustomizeAppDrawerBinding ->
    showHeadingSwitchListener(corePrefsRepo)
        .also(options.showHeadingsSwitchTitle::setOnClickListener)
        .also(options.showHeadingsSwitchSubtitle::setOnClickListener)
        .also(options.showHeadingsSwitchToggle::setOnClickListener)
    corePrefsRepo.observe(updateShowHeadingSwitchLayout(options))
}

fun setupVisibleAppsButton(options: CustomizeAppDrawerBinding) = Navigation
    .createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment)
    .also(options.visibleApps::setOnClickListener)

private fun getSearchFieldOptionButtonPositionText(resources: Resources, corePrefs: CorePreferences) = corePrefs
    .searchBarPosition.number.let {
        resources.getStringArray(R.array.search_bar_position_array)[it].lowercase()
    }

private fun getSearchFieldOptionsButtonKeyboardText(resources: Resources, corePrefs: CorePreferences) =
    when (corePrefs.activateKeyboardInDrawer) {
        true -> R.string.shown
        false -> R.string.hidden
    }.let(resources::getText)

private fun getSearchFieldOptionButtonSubtitle(corePrefs: CorePreferences, resources: Resources): CharSequence {
    if (corePrefs.hasShowSearchBar() && !corePrefs.showSearchBar) {
        return resources.getText(R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_hidden)
    }
    return resources.getString(
        R.string.customize_app_drawer_fragment_search_field_options_subtitle_status_shown,
        getSearchFieldOptionButtonPositionText(resources, corePrefs),
        getSearchFieldOptionsButtonKeyboardText(resources, corePrefs)
    )
}

fun setupSearchFieldOptionsButton(corePrefsRepo: DataRepository<CorePreferences>, resources: Resources) =
    { options: CustomizeAppDrawerBinding ->
        Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customizeSearchFieldFragment)
            .also(options.searchFieldOptionsTitle::setOnClickListener)
            .also(options.searchFieldOptionsSubtitle::setOnClickListener)
        corePrefsRepo.observe {
            options.searchFieldOptionsSubtitle.text = getSearchFieldOptionButtonSubtitle(it, resources)
        }
    }
