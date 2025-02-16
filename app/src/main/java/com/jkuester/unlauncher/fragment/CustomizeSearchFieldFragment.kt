package com.jkuester.unlauncher.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.setupBackButton
import com.jkuester.unlauncher.bindings.setupKeyboardSwitch
import com.jkuester.unlauncher.bindings.setupSearchAllAppsSwitch
import com.jkuester.unlauncher.bindings.setupSearchBarPositionOption
import com.jkuester.unlauncher.bindings.setupShowSearchBarSwitch
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerFragmentSearchFieldOptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeSearchFieldFragment : Fragment() {
    @Inject
    @WithFragmentLifecycle
    lateinit var corePrefsRepo: CorePreferencesRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_app_drawer_fragment_search_field_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = super
        .onViewCreated(view, savedInstanceState)
        .also {
            CustomizeAppDrawerFragmentSearchFieldOptionsBinding
                .bind(view)
                .also(setupBackButton(requireActivity()))
                .also(setupShowSearchBarSwitch(corePrefsRepo))
                .also(setupSearchBarPositionOption(corePrefsRepo, childFragmentManager, resources))
                .also(setupKeyboardSwitch(corePrefsRepo))
                .also(setupSearchAllAppsSwitch(corePrefsRepo))
        }
}
