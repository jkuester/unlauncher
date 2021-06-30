package com.sduduzog.slimlauncher.ui.main

import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.BuildConfig
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.AddAppAdapter
import com.sduduzog.slimlauncher.adapters.HomeAdapter
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

                // Since the app previously supported more than 6 apps, we need this as a transition to only
                // allowing 6 home apps. This can be removed in the future when it is likely everyone has
                // upgraded to a version that only supports 6 home apps.
                if(apps.size > 6) {
                    apps.subList(6, apps.size).forEach(viewModel::remove)
                }
            }
        })

        setEventListeners()

        // Populate the app drawer
        val openAppAdapter = AddAppAdapter(this)
        app_drawer_fragment_list.adapter = openAppAdapter
        viewModel.addAppViewModel.apps.observe(viewLifecycleOwner, Observer {
            it?.let { apps ->
                openAppAdapter.setItems(apps)
            }
        })
        home_fragment.setTransitionListener(object : TransitionListener {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                // hide the keyboard and remove focus from the EditText when swiping back up
                if (currentId == motionLayout?.startState) {
                    resetAppDrawerEditText()
                    val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
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

        val leftButtonIcon = getQuickButtonIcon(R.string.prefs_settings_key_quick_button_left_icon_id, R.drawable.ic_call)
        home_fragment_call.setImageResource(leftButtonIcon)
        if(leftButtonIcon != R.drawable.ic_empty) {
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

        val centerButtonIcon = getQuickButtonIcon(R.string.prefs_settings_key_quick_button_center_icon_id, R.drawable.ic_cog)
        home_fragment_options.setImageResource(centerButtonIcon)
        if(centerButtonIcon != R.drawable.ic_empty) {
            home_fragment_options.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_optionsFragment))
        }

        val rightButtonIcon = getQuickButtonIcon(R.string.prefs_settings_key_quick_button_right_icon_id, R.drawable.ic_photo_camera)
        home_fragment_camera.setImageResource(rightButtonIcon)
        if(rightButtonIcon != R.drawable.ic_empty) {
            home_fragment_camera.setOnClickListener {
                try {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                    launchActivity(it, intent)
                } catch (e: Exception) {
                    // Do nothing
                }
            }
        }

        app_drawer_edit_text.addTextChangedListener(onTextChangeListener)
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

    override fun onAppClicked(app: App) {
        launchApp(app.packageName, app.activityName, app.userSerial)
        home_fragment.transitionToStart()
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

    private fun getQuickButtonIcon(buttonPrefKey: Int, defaultIconId: Int): Int {
        return context?.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
                ?.getInt(getString(buttonPrefKey), defaultIconId) ?: defaultIconId
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
