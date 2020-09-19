package com.sduduzog.slimlauncher.ui.options

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.BuildConfig
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.AddAppAdapter
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.AddAppViewModel
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.OnAppClickedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.add_app_fragment.*

@AndroidEntryPoint
open class AddAppFragment : BaseFragment(), OnAppClickedListener {

    override fun getFragmentView(): ViewGroup = add_app_fragment

    private  val viewModel: AddAppViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_app_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = AddAppAdapter(this)

        add_app_fragment_list.adapter = adapter

        viewModel.apps.observe(viewLifecycleOwner, Observer {
            it?.let { apps ->
                adapter.setItems(apps)
                add_app_fragment_progress_bar.visibility = View.GONE
            } ?: run {
                add_app_fragment_progress_bar.visibility = View.VISIBLE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.setInstalledApps(getInstalledApps())
        viewModel.filterApps("")
        add_app_fragment_edit_text.addTextChangedListener(onTextChangeListener)
    }

    override fun onPause() {
        super.onPause()
        add_app_fragment_edit_text?.removeTextChangedListener(onTextChangeListener)
    }

    private val onTextChangeListener: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            // Do nothing
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.filterApps(s.toString())
        }
    }

    override fun onAppClicked(app: App) {
        viewModel.addAppToHomeScreen(app)
        Navigation.findNavController(add_app_fragment).popBackStack()
    }

    private fun getInstalledApps(): List<App> {
        val list = mutableListOf<App>()

        val manager = requireContext().getSystemService(Context.USER_SERVICE) as UserManager
        val launcher = requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val myUserHandle = Process.myUserHandle()

        for (profile in manager.userProfiles) {
            val prefix = if (profile.equals(myUserHandle)) "" else "\uD83C\uDD46 " //Unicode for boxed w
            val profileSerial = manager.getSerialNumberForUser(profile)

            for (activityInfo in launcher.getActivityList(null, profile)) {
                val app = App(
                        appName = prefix + activityInfo.label.toString(),
                        packageName = activityInfo.applicationInfo.packageName,
                        activityName = activityInfo.name,
                        userSerial = profileSerial
                )
                list.add(app)
            }
        }

        list.sortBy{it.appName}

        val filter = mutableListOf<String>()
        filter.add(BuildConfig.APPLICATION_ID)
        return list.filterNot { filter.contains(it.packageName) }
    }
}
