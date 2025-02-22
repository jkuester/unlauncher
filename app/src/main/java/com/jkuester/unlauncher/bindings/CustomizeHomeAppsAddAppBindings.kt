package com.jkuester.unlauncher.bindings

import androidx.activity.ComponentActivity
import com.jkuester.unlauncher.adapter.CustomizeHomeAppsAddAppAdapter
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.databinding.CustomizeHomeAppsAddAppBinding

fun setupAddAppBackButton(activity: ComponentActivity) = { options: CustomizeHomeAppsAddAppBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

fun setupAddAppsList(appsRepo: DataRepository<UnlauncherApps>, activity: ComponentActivity) =
    { options: CustomizeHomeAppsAddAppBinding ->
        options.addAppList.adapter = CustomizeHomeAppsAddAppAdapter(appsRepo, activity)
    }
