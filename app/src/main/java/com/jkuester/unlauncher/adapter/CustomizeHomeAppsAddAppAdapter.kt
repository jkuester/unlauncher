package com.jkuester.unlauncher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.addHomeApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.R

class CustomizeHomeAppsAddAppAdapter(
    private val appsRepo: DataRepository<UnlauncherApps>,
    private val activity: ComponentActivity
) : RecyclerView.Adapter<CustomizeHomeAppsAddAppAdapter.ViewHolder>() {
    private var apps = appsRepo.get().appsList.filter { !it.hasHomeAppIndex() }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.appName.text = item.displayName
        holder.appName.setOnClickListener {
            appsRepo.updateAsync(addHomeApp(item))
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater
        .from(parent.context)
        .inflate(R.layout.customize_home_apps_add_app_list_item, parent, false)
        .let { view -> ViewHolder(view) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.list_item_text)
    }
}
