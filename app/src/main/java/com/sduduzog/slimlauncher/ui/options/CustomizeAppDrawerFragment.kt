package com.sduduzog.slimlauncher.ui.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val unlauncherAppsRepo = getUnlauncherDataSource().unlauncherAppsRepo
        customize_app_drawer_fragment_app_list.adapter = CustomizeAppDrawerAppsAdapter(viewLifecycleOwner, unlauncherAppsRepo)
        unlauncherAppsRepo.liveData().observe(viewLifecycleOwner, {
            it?.let {
                customize_app_drawer_fragment_app_progress_bar.visibility = View.GONE
            } ?: run {
                customize_app_drawer_fragment_app_progress_bar.visibility = View.VISIBLE
            }
        })

        val settings = requireContext().getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
        customize_app_drawer_open_keyboard_switch.setOnCheckedChangeListener { _, checked ->
            settings.edit()
                .putBoolean(getString(R.string.prefs_settings_key_open_keyboard), checked)
                .apply()
        }
        customize_app_drawer_open_keyboard_switch.isChecked = settings.getBoolean(getString(R.string.prefs_settings_key_open_keyboard), true)
    }
}
