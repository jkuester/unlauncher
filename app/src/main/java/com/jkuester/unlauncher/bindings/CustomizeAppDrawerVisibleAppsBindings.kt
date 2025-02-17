package com.jkuester.unlauncher.bindings

import androidx.activity.ComponentActivity
import com.jkuester.unlauncher.adapter.CustomizeAppDrawerVisibleAppsAdapter
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.databinding.CustomizeAppDrawerVisibleAppsBinding

fun setupVisibleAppsBackButton(activity: ComponentActivity) = { options: CustomizeAppDrawerVisibleAppsBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

fun setupVisibleAppsList(appsRepo: DataRepository<UnlauncherApps>) = { options: CustomizeAppDrawerVisibleAppsBinding ->
    options.customizeAppDrawerVisibleAppsList.adapter = CustomizeAppDrawerVisibleAppsAdapter(appsRepo)
}
