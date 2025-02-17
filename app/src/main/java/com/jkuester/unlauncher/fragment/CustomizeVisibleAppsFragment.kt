package com.jkuester.unlauncher.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.setupVisibleAppsBackButton
import com.jkuester.unlauncher.bindings.setupVisibleAppsList
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerVisibleAppsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeVisibleAppsFragment : Fragment() {
    @Inject
    lateinit var iActivity: ComponentActivity
    @Inject
    lateinit var unlauncherAppsRepo: UnlauncherAppsRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_app_drawer_visible_apps, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomizeAppDrawerVisibleAppsBinding
            .bind(view)
            .also(setupVisibleAppsBackButton(iActivity))
            .also(setupVisibleAppsList(unlauncherAppsRepo))
    }
}
