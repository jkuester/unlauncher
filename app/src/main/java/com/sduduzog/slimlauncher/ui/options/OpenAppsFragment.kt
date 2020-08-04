package com.sduduzog.slimlauncher.ui.options

import android.content.ComponentName
import android.content.Intent
import androidx.navigation.fragment.NavHostFragment
import com.sduduzog.slimlauncher.data.model.App

class OpenAppsFragment : AddAppFragment() {

    override fun onAppClicked(app: App) {
        try {
            val intent = Intent()
            val name = ComponentName(app.packageName, app.activityName)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            intent.component = name

            intent.resolveActivity(activity!!.packageManager)?.let {
                launchActivity(getFragmentView(), intent)
            }
        } catch (e: Exception) {
        }
        NavHostFragment.findNavController(this).popBackStack();
    }
}