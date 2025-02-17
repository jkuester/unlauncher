package com.jkuester.unlauncher.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.setupCustomizeAppDrawerBackButton
import com.jkuester.unlauncher.bindings.setupSearchFieldOptionsButton
import com.jkuester.unlauncher.bindings.setupShowHeadingSwitch
import com.jkuester.unlauncher.bindings.setupVisibleAppsButton
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeAppDrawerFragment : Fragment() {
    @Inject
    lateinit var iActivity: ComponentActivity
    @Inject
    lateinit var iResources: Resources
    @Inject @WithFragmentLifecycle
    lateinit var corePreferencesRepo: DataRepository<CorePreferences>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_app_drawer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomizeAppDrawerBinding
            .bind(view)
            .also(::setupVisibleAppsButton)
            .also(setupCustomizeAppDrawerBackButton(iActivity))
            .also(setupSearchFieldOptionsButton(corePreferencesRepo, iResources))
            .also(setupShowHeadingSwitch(corePreferencesRepo))
    }
}
