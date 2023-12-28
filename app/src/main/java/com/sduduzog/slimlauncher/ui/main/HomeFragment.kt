package com.sduduzog.slimlauncher.ui.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Bundle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.jkuester.unlauncher.datastore.SearchBarPosition
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.AppDrawerAdapter
import com.sduduzog.slimlauncher.adapters.HomeAdapter
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.datasource.quickbuttonprefs.QuickButtonPreferencesRepository
import com.sduduzog.slimlauncher.models.HomeApp
import com.sduduzog.slimlauncher.models.MainViewModel
import com.sduduzog.slimlauncher.ui.dialogs.RenameAppDisplayNameDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.OnLaunchAppListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment_default.*
import kotlinx.android.synthetic.main.home_fragment_content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(), OnLaunchAppListener {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    private val viewModel: MainViewModel by viewModels()

    private lateinit var receiver: BroadcastReceiver
    private lateinit var appDrawerAdapter: AppDrawerAdapter
    private lateinit var uninstallAppLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uninstallAppLauncher = registerForActivityResult(StartActivityForResult()) { refreshApps() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val coreRepository = unlauncherDataSource.corePreferencesRepo
        val layout = when (coreRepository.get().searchBarPosition) {
            SearchBarPosition.bottom -> R.layout.home_fragment_bottom
            SearchBarPosition.UNRECOGNIZED,
            SearchBarPosition.top -> R.layout.home_fragment_default
            else -> R.layout.home_fragment_default
        }
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingsKey = getString(R.string.prefs_settings)
        val alignmentKey: String = getString(R.string.prefs_settings_alignment)
        val preferences = requireContext().getSharedPreferences(settingsKey, Context.MODE_PRIVATE)
        val alignment = preferences.getInt(alignmentKey, 3)

        val adapter1 = HomeAdapter(this, alignment)
        val adapter2 = HomeAdapter(this, alignment)
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

        appDrawerAdapter = AppDrawerAdapter(
            AppDrawerListener(),
            viewLifecycleOwner,
            unlauncherAppsRepo,
            unlauncherDataSource.corePreferencesRepo
        )

        setEventListeners()

        app_drawer_fragment_list.adapter = appDrawerAdapter

        val showSearchBar = unlauncherDataSource.corePreferencesRepo.showSearchField
        app_drawer_edit_text.visibility = if (showSearchBar) View.VISIBLE else View.GONE
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

        refreshApps()
        if (!::appDrawerAdapter.isInitialized) {
            appDrawerAdapter.setAppFilter()
        }

        // scroll back to the top if user returns to this fragment
        val layoutManager = app_drawer_fragment_list.layoutManager as LinearLayoutManager
        if (layoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
            app_drawer_fragment_list.scrollToPosition(0)
        }
    }

    private fun refreshApps() {
        val installedApps = getInstalledApps()
        lifecycleScope.launch(Dispatchers.IO) {
            unlauncherDataSource.unlauncherAppsRepo.setApps(installedApps)
            viewModel.filterHomeApps(installedApps)
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
                val leftButtonIcon = QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.leftButton.iconId)
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

                val centerButtonIcon = QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.centerButton.iconId)
                home_fragment_options.setImageResource(centerButtonIcon)
                if (centerButtonIcon != R.drawable.ic_empty) {
                    home_fragment_options.setOnClickListener(
                        Navigation.createNavigateOnClickListener(
                            R.id.action_homeFragment_to_optionsFragment
                        )
                    )
                }

                val rightButtonIcon = QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.rightButton.iconId)
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

        app_drawer_edit_text.setOnEditorActionListener { _, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_DONE && appDrawerAdapter.itemCount > 0) {
                    val firstApp = appDrawerAdapter.getFirstApp()
                    launchApp(firstApp.packageName, firstApp.className, firstApp.userSerial)
                    home_fragment.transitionToStart()
                    true
                } else {
                    false
                }
            }

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
                        val repository = unlauncherDataSource.corePreferencesRepo
                        val showSearchField = repository.showSearchField
                        val activateKeyboard = repository.get().activateKeyboardInDrawer

                        // Check for preferences to open the keyboard
                        // only if the search field is shown
                        if (showSearchField && activateKeyboard) {
                            app_drawer_edit_text.requestFocus()
                            // show the keyboard and set focus to the EditText when swiping down
                            inputMethodManager.showSoftInput(
                                app_drawer_edit_text,
                                InputMethodManager.SHOW_IMPLICIT
                            )
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
        fun onAppLongClicked(app : UnlauncherApp, view: View) : Boolean {
            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.app_long_press_menu)

            popupMenu.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.open -> {
                        onAppClicked(app)
                    }
                    R.id.info -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:" + app.packageName)
                        startActivity(intent)
                    }
                    R.id.hide -> {
                        unlauncherDataSource.unlauncherAppsRepo.updateDisplayInDrawer(app, false)
                        Toast.makeText(context, "Unhide under Unlauncher's Options > Customize Drawer > Visible Apps", Toast.LENGTH_LONG).show()
                    }
                    R.id.rename -> {
                        RenameAppDisplayNameDialog.getInstance(app, unlauncherDataSource.unlauncherAppsRepo).show(childFragmentManager, "AppListAdapter")
                    }
                    R.id.uninstall -> {
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = Uri.parse("package:" + app.packageName)
                        uninstallAppLauncher.launch(intent)
                        //appDrawerAdapter.notifyDataSetChanged()
                        // TODO: Handle the case when this is done for system apps
                    }
                }
                true
            }

            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)

            popupMenu.show()
            return true
        }

        fun onAppClicked(app: UnlauncherApp) {
            launchApp(app.packageName, app.className, app.userSerial)
            home_fragment.transitionToStart()
        }
    }
}
