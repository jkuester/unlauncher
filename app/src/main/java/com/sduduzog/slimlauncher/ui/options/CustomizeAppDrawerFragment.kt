package com.sduduzog.slimlauncher.ui.options

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.CustomizeAppDrawerAppsAdapter
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.AddAppViewModel
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.OnAppClickedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.*

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment(), OnAppClickedListener {

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    private val viewModel: AddAppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO need to try and clear out any hidden apps that no longer exist

        return inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = CustomizeAppDrawerAppsAdapter(this)

        customize_app_drawer_fragment_app_list.adapter = adapter

        viewModel.apps.observe(viewLifecycleOwner, Observer {
            it?.let { apps ->
                // TODO Here is maybe where we should set the checks...
                adapter.setItems(apps, listOf())
                customize_app_drawer_fragment_app_progress_bar.visibility = View.GONE
            } ?: run {
                customize_app_drawer_fragment_app_progress_bar.visibility = View.VISIBLE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.setInstalledApps(getInstalledApps())
        viewModel.filterApps("")
    }

    override fun onPause() {
        super.onPause()

        val inputMethodManager =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onAppClicked(app: App) {
        // TODO add the entry to the db...
//        viewModel.addAppToHomeScreen(app)
//        Navigation.findNavController(customize_app_drawer_fragment).popBackStack()
    }
}
