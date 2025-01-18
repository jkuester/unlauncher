package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.CustomizeAppDrawerAppsAdapter
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerAppListFragmentBinding
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeAppDrawerAppListFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherAppsRepo: UnlauncherAppsRepository

    override fun getFragmentView(): ViewGroup = CustomizeAppDrawerAppListFragmentBinding.bind(
        requireView()
    ).customizeAppDrawerFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_app_drawer_app_list_fragment, container, false)

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val customiseAppDrawerAppListFragment = CustomizeAppDrawerAppListFragmentBinding.bind(
            requireView()
        )
        customiseAppDrawerAppListFragment.customizeAppDrawerFragmentAppList.adapter =
            CustomizeAppDrawerAppsAdapter(viewLifecycleOwner, unlauncherAppsRepo)
        unlauncherAppsRepo.liveData().observe(viewLifecycleOwner) {
            it?.let {
                customiseAppDrawerAppListFragment.customizeAppDrawerFragmentAppProgressBar
                    .visibility = View.GONE
            } ?: run {
                customiseAppDrawerAppListFragment.customizeAppDrawerFragmentAppProgressBar
                    .visibility = View.VISIBLE
            }
        }
        customiseAppDrawerAppListFragment.customizeAppDrawerFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
