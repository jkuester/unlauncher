package com.jkuester.unlauncher.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.bindings.setupAddHomeAppButton
import com.jkuester.unlauncher.bindings.setupCustomizeQuickButtonsBackButton
import com.jkuester.unlauncher.bindings.setupHomeAppsList
import com.jkuester.unlauncher.bindings.setupQuickButtonIcons
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeQuickButtonsFragment : Fragment() {
    @Inject
    lateinit var iActivity: ComponentActivity
    @Inject
    lateinit var iFragmentManager: FragmentManager
    @Inject
    lateinit var quickButtonPreferencesRepo: DataRepository<QuickButtonPreferences>
    @Inject
    lateinit var appsRepo: DataRepository<UnlauncherApps>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_quick_buttons, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomizeQuickButtonsBinding
            .bind(view)
            .also(setupCustomizeQuickButtonsBackButton(iActivity))
            .also(setupQuickButtonIcons(quickButtonPreferencesRepo, iFragmentManager))
            .also(::setupAddHomeAppButton)
            .also(setupHomeAppsList(appsRepo))
    }
}
