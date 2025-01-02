package com.sduduzog.slimlauncher.data.model

import com.sduduzog.slimlauncher.models.HomeApp

// appType == 0 => app              => defaultVisible == true
// appType == 1 => pinned shortcuts => defaultVisible == true
// appType == 2 => other shortcuts  => defaultVisible == false
data class App(
    val appType: Int,
    val appName: String,
    val packageName: String,
    val activityName: String,
    val userSerial: Long
) {
    companion object {
        fun from(homeApp: HomeApp) = App(
            appType = homeApp.appType,
            appName = homeApp.appName,
            packageName = homeApp.packageName,
            activityName = homeApp.activityName,
            userSerial = homeApp.userSerial
        )
    }
}
