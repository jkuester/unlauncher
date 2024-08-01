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

        // Remember the shortcuts: Öffi sends the same one three times.
        var have = mutableSetOf<String>()

        for (profile in manager.userProfiles) {
            // Unicode for boxed w
            val prefix = if (profile.equals(myUserHandle)) "" else "\uD83C\uDD46 "
            val profileSerial = manager.getSerialNumberForUser(profile)

            for (activityInfo in launcher.getActivityList(null, profile)) {
                // Handle app itself:
                var label = activityInfo.label.toString()
                val packName = activityInfo.applicationInfo.packageName
                val app = App(
                    appType = 0,
                    appName = prefix + label,
                    packageName = packName,
                    activityName = activityInfo.name,
                    userSerial = profileSerial
                )
                list.add(app)

                // Now get pinned shortcuts:
                try {
                    val query = LauncherApps.ShortcutQuery()
                    query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                    query.setPackage(packName)
                    launcher.getShortcuts(query, myUserHandle)?.map { s ->
                        val shortCut = App(
                            appType = 1,
                            appName = prefix + label + ": " + s.shortLabel.toString(),
                            packageName = s.`package`,
                            activityName = s.id,
                            userSerial = profileSerial
                        )
                        val key = s.`package` + ' ' + s.id
                        if (have.add(key)) {
                            list.add(shortCut)
                        }
                    }
                } catch (e: SecurityException) {
                    // ignore
                }

                // Now get other shortcuts:
                try {
                    val query = LauncherApps.ShortcutQuery()
                    query.setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    )
                    query.setPackage(packName)
                    launcher.getShortcuts(query, myUserHandle)?.map { s ->
                        val shortCut = App(
                            appType = 2,
                            appName = prefix + label + ": " + s.shortLabel.toString(),
                            packageName = s.`package`,
                            activityName = s.id,
                            userSerial = profileSerial
                        )
                        val key = s.`package` + ' ' + s.id
                        if (have.add(key)) {
                            list.add(shortCut)
                        }
                    }
                } catch (e: SecurityException) {
                    // ignore
                }
            }
        }

        list.sortBy { it.appName }

        val filter = mutableListOf<String>()
        filter.add(BuildConfig.APPLICATION_ID)
        return list.filterNot { filter.contains(it.packageName) }
    }
}
