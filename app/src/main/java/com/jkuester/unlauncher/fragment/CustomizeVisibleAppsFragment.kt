package com.jkuester.unlauncher.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.CustomizeAppDrawerAppsAdapter
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerVisibleAppsBinding
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeVisibleAppsFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherAppsRepo: UnlauncherAppsRepository

    override fun getFragmentView(): ViewGroup = CustomizeAppDrawerVisibleAppsBinding.bind(
        requireView()
    ).customizeAppDrawerFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_app_drawer_visible_apps, container, false)

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val customiseAppDrawerAppListFragment = CustomizeAppDrawerVisibleAppsBinding.bind(
            requireView()
        )
        customiseAppDrawerAppListFragment.customizeAppDrawerFragmentAppList.adapter =
            CustomizeAppDrawerAppsAdapter(viewLifecycleOwner, unlauncherAppsRepo)
        unlauncherAppsRepo.observe {
            when (it.appsList.isEmpty()) {
                true -> customiseAppDrawerAppListFragment.customizeAppDrawerFragmentAppProgressBar
                    .visibility = View.VISIBLE
                false -> customiseAppDrawerAppListFragment.customizeAppDrawerFragmentAppProgressBar
                    .visibility = View.GONE
            }
        }
        customiseAppDrawerAppListFragment.customizeAppDrawerFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
