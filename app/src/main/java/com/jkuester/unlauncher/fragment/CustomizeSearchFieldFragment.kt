package com.jkuester.unlauncher.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.bindings.setupBackButton
import com.jkuester.unlauncher.bindings.setupKeyboardSwitch
import com.jkuester.unlauncher.bindings.setupSearchAllAppsSwitch
import com.jkuester.unlauncher.bindings.setupSearchBarPositionOption
import com.jkuester.unlauncher.bindings.setupShowSearchBarSwitch
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerSearchFieldOptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeSearchFieldFragment : Fragment() {
    @Inject
    lateinit var iActivity: ComponentActivity
    @Inject
    lateinit var iResources: Resources
    @Inject
    lateinit var iFragmentManager: FragmentManager
    @Inject @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_app_drawer_search_field_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomizeAppDrawerSearchFieldOptionsBinding
            .bind(view)
            .also(setupBackButton(iActivity))
            .also(setupShowSearchBarSwitch(corePrefsRepo))
            .also(setupSearchBarPositionOption(corePrefsRepo, iFragmentManager, iResources))
            .also(setupKeyboardSwitch(corePrefsRepo))
            .also(setupSearchAllAppsSwitch(corePrefsRepo))
    }
}
