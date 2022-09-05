package com.sduduzog.slimlauncher.ui.main

import android.app.Activity
import android.content.*
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.AppDrawerAdapter
import com.sduduzog.slimlauncher.adapters.HomeAdapter
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.models.HomeApp
import com.sduduzog.slimlauncher.models.MainViewModel
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.OnLaunchAppListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(), OnLaunchAppListener {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    private val viewModel: MainViewModel by viewModels()

    private lateinit var receiver: BroadcastReceiver
    private lateinit var appDrawerAdapter: AppDrawerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.home_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter1 = HomeAdapter(this)
        val adapter2 = HomeAdapter(this)
        home_fragment_list.adapter = adapter1
        home_fragment_list_exp.adapter = adapter2

        val unlauncherAppsRepo = unlauncherDataSource.unlauncherAppsRepo

        viewModel.apps.observe(viewLifecycleOwner) { list ->
            list?.let { apps ->
                adapter1.setItems(apps.filter {
                    it.sortingIndex < 3
                })
                adapter2.setItems(apps.filter {
                    it.sortingIndex >= 3
                })

                // Set the home apps in the Unlauncher data
                lifecycleScope.launch {
                    unlauncherAppsRepo.setHomeApps(apps)
                }
            }
        }

        appDrawerAdapter =
            AppDrawerAdapter(AppDrawerListener(), viewLifecycleOwner, unlauncherAppsRepo)

        setEventListeners()

        app_drawer_fragment_list.adapter = appDrawerAdapter
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

        lifecycleScope.launch(Dispatchers.IO) {
            unlauncherDataSource.unlauncherAppsRepo.setApps(getInstalledApps())
        }
        if (!::appDrawerAdapter.isInitialized) {
            appDrawerAdapter.setAppFilter()
        }

        // scroll back to the top if user returns to this fragment
        val layoutManager = app_drawer_fragment_list.layoutManager as LinearLayoutManager
        if (layoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
            app_drawer_fragment_list.scrollToPosition(0)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(receiver)
        resetAppDrawerEditText()
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

        unlauncherDataSource.quickButtonPreferencesRepo.liveData()
            .observe(viewLifecycleOwner) { prefs ->
                val leftButtonIcon = prefs.leftButton.iconId
                home_fragment_call.setImageResource(leftButtonIcon)
                if (leftButtonIcon != R.drawable.ic_empty) {
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
                }

                val centerButtonIcon = prefs.centerButton.iconId
                home_fragment_options.setImageResource(centerButtonIcon)
                if (centerButtonIcon != R.drawable.ic_empty) {
                    home_fragment_options.setOnClickListener(
                        Navigation.createNavigateOnClickListener(
                            R.id.action_homeFragment_to_optionsFragment
                        )
                    )
                }

                val rightButtonIcon = prefs.rightButton.iconId
                home_fragment_camera.setImageResource(rightButtonIcon)
                if (rightButtonIcon != R.drawable.ic_empty) {
                    home_fragment_camera.setOnClickListener {
                        try {
                            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                            launchActivity(it, intent)
                        } catch (e: Exception) {
                            // Do nothing
                        }
                    }
                }
            }

        app_drawer_edit_text.addTextChangedListener(appDrawerAdapter.searchBoxListener)

        home_fragment.setTransitionListener(object : TransitionListener {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

                when (currentId) {
                    motionLayout?.startState -> {
                        // hide the keyboard and remove focus from the EditText when swiping back up
                        resetAppDrawerEditText()
                        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
                    }

                    motionLayout?.endState -> {
                        // Check for preferences to open the keyboard
                        unlauncherDataSource.corePreferencesRepo.liveData().observe(viewLifecycleOwner) {
                            if (it.activateKeyboardInDrawer) {
                                // show the keyboard and set focus to the EditText when swiping down
                                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                                app_drawer_edit_text.requestFocus()
                            }
                        }
                    }
                }
            }

            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {
                // do nothing
            }

            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
                // do nothing
            }

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                // do nothing
            }
        })
    }

    fun updateClock() {
        val active = context?.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
                ?.getInt(getString(R.string.prefs_settings_key_time_format), 0)
        val date = Date()

        val currentLocale = Locale.getDefault()
        val fWatchTime = when(active) {
            1 -> SimpleDateFormat("H:mm", currentLocale)
            2 -> SimpleDateFormat("h:mm aa", currentLocale)
            else -> DateFormat.getTimeInstance(DateFormat.SHORT)
        }
        home_fragment_time.text = fWatchTime.format(date)


        val fWatchDate = SimpleDateFormat("EEE, MMM dd", currentLocale)
        home_fragment_date.text = fWatchDate.format(date)
    }

    override fun onLaunch(app: HomeApp, view: View) {
        launchApp(app.packageName, app.activityName, app.userSerial)
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

    private fun launchApp(packageName: String, activityName: String, userSerial: Long) {
        try {
            val manager = requireContext().getSystemService(Context.USER_SERVICE) as UserManager
            val launcher = requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

            val componentName = ComponentName(packageName, activityName)
            val userHandle = manager.getUserForSerialNumber(userSerial)

            launcher.startMainActivity(componentName, userHandle, view?.clipBounds, null)
        } catch (e: Exception) {
            // Do no shit yet
        }
    }

    private fun resetAppDrawerEditText() {
        app_drawer_edit_text.clearComposingText()
        app_drawer_edit_text.setText("")
        app_drawer_edit_text.clearFocus()
    }

    inner class AppDrawerListener {
        fun onAppClicked(app: UnlauncherApp) {
            launchApp(app.packageName, app.className, app.userSerial)
            home_fragment.transitionToStart()
        }
    }
}
