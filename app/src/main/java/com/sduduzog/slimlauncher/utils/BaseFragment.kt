package com.sduduzog.slimlauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserManager
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.sduduzog.slimlauncher.BuildConfig
import com.sduduzog.slimlauncher.data.model.App
import javax.inject.Inject

abstract class BaseFragment : Fragment(), ISubscriber {
    @Inject
    lateinit var systemUiManager: SystemUiManager

    abstract fun getFragmentView(): ViewGroup

    override fun onResume() {
        super.onResume()
        systemUiManager.setSystemUiColors()
    }

    override fun onStart() {
        super.onStart()
        with(activity as IPublisher) {
            this.attachSubscriber(this@BaseFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        with(activity as IPublisher) {
            this.detachSubscriber(this@BaseFragment)
        }
    }

    protected fun launchActivity(view: View, intent: Intent) {
        val left = 0
        val top = 0
        val width = view.measuredWidth
        val height = view.measuredHeight
        val opts = ActivityOptionsCompat.makeClipRevealAnimation(view, left, top, width, height)
        startActivity(intent, opts.toBundle())
    }

    open fun onBack(): Boolean = false

    open fun onHome() {}

    protected fun getInstalledApps(): List<App> {
        val list = mutableListOf<App>()

        val manager = requireContext().getSystemService(Context.USER_SERVICE) as UserManager
        val launcher = requireContext().getSystemService(
            Context.LAUNCHER_APPS_SERVICE
        ) as LauncherApps
        val myUserHandle = Process.myUserHandle()

        for (profile in manager.userProfiles) {
            // Unicode for boxed w
            val prefix = if (profile.equals(myUserHandle)) "" else "\uD83C\uDD46 "
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

        list.sortBy { it.appName }

        val filter = mutableListOf<String>()
        filter.add(BuildConfig.APPLICATION_ID)
        return list.filterNot { filter.contains(it.packageName) }
    }
}
