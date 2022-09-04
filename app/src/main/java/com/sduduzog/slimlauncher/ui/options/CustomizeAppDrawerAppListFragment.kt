package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.CustomizeAppDrawerAppsAdapter
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_app_list_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeAppDrawerAppListFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.customize_app_drawer_app_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val unlauncherAppsRepo = unlauncherDataSource.unlauncherAppsRepo
        customize_app_drawer_fragment_app_list.adapter =
            CustomizeAppDrawerAppsAdapter(viewLifecycleOwner, unlauncherAppsRepo)
        unlauncherAppsRepo.liveData().observe(viewLifecycleOwner) {
            it?.let {
                customize_app_drawer_fragment_app_progress_bar.visibility = View.GONE
            } ?: run {
                customize_app_drawer_fragment_app_progress_bar.visibility = View.VISIBLE
            }
        }
    }
}