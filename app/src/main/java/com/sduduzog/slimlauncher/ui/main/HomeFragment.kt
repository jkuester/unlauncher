package com.sduduzog.slimlauncher.ui.main

import android.content.*
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.sduduzog.slimlauncher.BuildConfig
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.HomeAdapter
import com.sduduzog.slimlauncher.adapters.OpenAppAdapter
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.models.HomeApp
import com.sduduzog.slimlauncher.models.MainViewModel
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.OnAppClickedListener
import com.sduduzog.slimlauncher.utils.OnLaunchAppListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment(private val viewModel: MainViewModel) : BaseFragment(), OnLaunchAppListener, OnAppClickedListener {

    private lateinit var receiver: BroadcastReceiver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter1 = HomeAdapter(this)
        val adapter2 = HomeAdapter(this)
        home_fragment_list.adapter = adapter1
        home_fragment_list_exp.adapter = adapter2

        viewModel.apps.observe(viewLifecycleOwner, Observer { list ->
            list?.let { apps ->
                adapter1.setItems(apps.filter {
                    it.sortingIndex < 3
                })
                adapter2.setItems(apps.filter {
                    it.sortingIndex >= 3
                })
            }
        })

        setEventListeners()
        home_fragment_options.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_optionsFragment))

        val openAppAdapter = OpenAppAdapter(this)
        app_drawer_fragment_list.adapter = openAppAdapter
        viewModel.addAppViewModel.apps.observe(viewLifecycleOwner, Observer {
            it?.let { apps ->
                openAppAdapter.setItems(apps)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        receiver = ClockReceiver()
        activity?.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun getFragmentView(): ViewGroup = home_fragment

    override fun onResume() {
        super.onResume()
        updateClock()

        viewModel.addAppViewModel.setInstalledApps(getInstalledApps())
        viewModel.addAppViewModel.filterApps("")
        app_drawer_edit_text.addTextChangedListener(onTextChangeListener)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
    }

    private fun setEventListeners() {

        home_fragment_time.setOnClickListener {
            try {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                launchActivity(it, intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                // Do nothing, we've failed :(
            }
        }

        home_fragment_date.setOnClickListener {
            try {
                val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                val intent = Intent(Intent.ACTION_VIEW, builder.build())
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                launchActivity(it, intent)
            } catch (e: ActivityNotFoundException) {
                // Do nothing, we've failed :(
            }
        }

        home_fragment_call.setOnClickListener { view ->
            try {
                val pm = context?.packageManager!!
                val intent = Intent(Intent.ACTION_DIAL)
                val componentName = intent.resolveActivity(pm)
                if (componentName == null) launchActivity(view, intent) else
                    pm.getLaunchIntentForPackage(componentName.packageName)?.let {
                        launchActivity(view, it)
                    } ?: run { launchActivity(view, intent) }
            } catch (e: Exception) {
                // Do nothing
            }
        }

        home_fragment_camera.setOnClickListener {
            try {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                launchActivity(it, intent)
            } catch (e: Exception) {
                // Do nothing
            }
        }
    }

    fun updateClock() {
        val active = context?.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
                ?.getInt(getString(R.string.prefs_settings_key_time_format), 0)
        val date = Date()

        val fWatchTime = when(active) {
            1 -> SimpleDateFormat("H:mm", Locale.ROOT)
            2 -> SimpleDateFormat("h:mm aa", Locale.ROOT)
            else -> DateFormat.getTimeInstance(DateFormat.SHORT)
        }
        home_fragment_time.text = fWatchTime.format(date)


        val fWatchDate = SimpleDateFormat("EEE, MMM dd", Locale.ROOT)
        home_fragment_date.text = fWatchDate.format(date)
    }

    override fun onLaunch(app: HomeApp, view: View) {
        try {
            val manager = requireContext().getSystemService(Context.USER_SERVICE) as UserManager
            val launcher = requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

            val componentName = ComponentName(app.packageName, app.activityName)
            val userHandle = manager.getUserForSerialNumber(app.userSerial)

            launcher.startMainActivity(componentName, userHandle, view.clipBounds, null)
        } catch (e: Exception) {
            // Do no shit yet
        }
    }

    override fun onBack(): Boolean {
        home_fragment.transitionToStart()
        return true
    }

    override fun onHome() {
        home_fragment.transitionToStart()
    }

    inner class ClockReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            updateClock()
        }
    }

    override fun onAppClicked(app: App) {
        try {
            val intent = Intent()
            val name = ComponentName(app.packageName, app.activityName)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            intent.component = name

            intent.resolveActivity(requireActivity().packageManager)?.let {
                launchActivity(getFragmentView(), intent)
            }
        } catch (e: Exception) {
        }
        home_fragment.transitionToStart()
//        NavHostFragment.findNavController(this).popBackStack();
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

    private val onTextChangeListener: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            // Do nothing
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.addAppViewModel.filterApps(s.toString())
        }
    }
}
