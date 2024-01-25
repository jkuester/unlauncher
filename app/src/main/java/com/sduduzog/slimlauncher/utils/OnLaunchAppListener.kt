package com.sduduzog.slimlauncher.utils

import android.view.View
import com.sduduzog.slimlauncher.models.HomeApp

interface OnLaunchAppListener {
    fun onLaunch(app: HomeApp, view: View)
}
