package com.sduduzog.slimlauncher.utils

import android.view.View
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp

interface OnLaunchAppListener {
    fun onLaunch(app: UnlauncherApp, view: View)
}
