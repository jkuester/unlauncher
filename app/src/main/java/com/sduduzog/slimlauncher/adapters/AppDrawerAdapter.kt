package com.sduduzog.slimlauncher.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datastore.UnlauncherApps
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.ui.main.HomeFragment

class AppDrawerAdapter(
    private val listener: HomeFragment.AppDrawerListener,
    lifecycleOwner: LifecycleOwner,
    appsRepo: UnlauncherAppsRepository
) : RecyclerView.Adapter<AppDrawerAdapter.ViewHolder>() {
    private var apps: UnlauncherApps = UnlauncherApps.getDefaultInstance()

    init {
        appsRepo.liveData().observe(lifecycleOwner, { unlauncherApps ->
            apps = unlauncherApps
//            notifyDataSetChanged() // TODO Figure out if we need this.
        })
    }

    override fun getItemCount(): Int = apps.appsCount

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps.getApps(position)
        holder.appName.text = item.displayName
        holder.itemView.setOnClickListener {
            listener.onAppClicked(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_app_fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appName: TextView = itemView.findViewById(R.id.aa_list_item_app_name)

        override fun toString(): String {
            return super.toString() + " '${appName.text}'"
        }
    }
}