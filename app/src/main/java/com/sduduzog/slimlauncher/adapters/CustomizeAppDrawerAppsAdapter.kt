package com.sduduzog.slimlauncher.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.jkuester.unlauncher.datasource.setDisplayInDrawer
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.sduduzog.slimlauncher.R

class CustomizeAppDrawerAppsAdapter(lifecycleOwner: LifecycleOwner, private val appsRepo: UnlauncherAppsRepository) :
    RecyclerView.Adapter<CustomizeAppDrawerAppsAdapter.ViewHolder>() {
    private var apps: UnlauncherApps = UnlauncherApps.getDefaultInstance()

    init {
        appsRepo.observe(lifecycleOwner, { unlauncherApps ->
            apps = unlauncherApps
        })
    }

    override fun getItemCount(): Int = apps.appsCount

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps.getApps(position)
        holder.appName.text = item.displayName
        holder.appName.isChecked = item.displayInDrawer
        holder.itemView.setOnClickListener {
            appsRepo.updateAsync(setDisplayInDrawer(item, holder.appName.isChecked))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.customize_app_drawer_fragment_app_list_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appName: CheckBox =
            itemView.findViewById(R.id.customize_app_drawer_fragment_app_list_item)

        override fun toString(): String = super.toString() + " '${appName.text}'"
    }
}
