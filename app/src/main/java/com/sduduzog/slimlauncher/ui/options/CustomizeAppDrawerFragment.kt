package com.sduduzog.slimlauncher.ui.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.CustomizeAppDrawerAppsAdapter
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.*

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment() {

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        customize_app_drawer_fragment_visible_apps.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment))

        val settings = requireContext().getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
        customize_app_drawer_open_keyboard_switch.setOnCheckedChangeListener { _, checked ->
            settings.edit()
                .putBoolean(getString(R.string.prefs_settings_key_open_keyboard), checked)
                .apply()
        }
        customize_app_drawer_open_keyboard_switch.isChecked = settings.getBoolean(getString(R.string.prefs_settings_key_open_keyboard), true)
    }
}
