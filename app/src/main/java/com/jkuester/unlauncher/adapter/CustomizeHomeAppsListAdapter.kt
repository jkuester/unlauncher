package com.jkuester.unlauncher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.createPopupMenuWithIcons
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.decrementHomeAppIndex
import com.jkuester.unlauncher.datasource.getHomeApps
import com.jkuester.unlauncher.datasource.incrementHomeAppIndex
import com.jkuester.unlauncher.datasource.removeHomeApp
import com.jkuester.unlauncher.datasource.unlauncherAppMatches
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.dialog.RenameAppDisplayNameDialog
import com.sduduzog.slimlauncher.R

class CustomizeHomeAppsListAdapter(
    private val appsRepo: DataRepository<UnlauncherApps>,
    private val fragmentManager: FragmentManager,
    override var apps: List<UnlauncherApp> = appsRepo.get().let(::getHomeApps)
) : RecyclerView.Adapter<CustomizeHomeAppsListAdapter.ViewHolder>(),
    UnlauncherAppListHolder {

    init {
        appsRepo.observe(notifyOfHomeAppChanges(this))
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.appName.text = item.displayName
        holder.appName.setOnClickListener(showCustomizeHomeAppsPopupMenu(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater
        .from(parent.context)
        .inflate(R.layout.main_fragment_list_item, parent, false)
        .let { view -> ViewHolder(view) }

    private fun showCustomizeHomeAppsPopupMenu(homeApp: UnlauncherApp) = OnClickListener { anchor ->
        val menu = createPopupMenuWithIcons(anchor.context, anchor)
        menu.inflate(R.menu.customize_home_apps_menu)
        menu.setOnMenuItemClickListener { menuItem ->
            val homeAppIndex = apps.first(unlauncherAppMatches(homeApp)).homeAppIndex
            when (menuItem.itemId) {
                R.id.rename -> RenameAppDisplayNameDialog(apps[homeAppIndex]).showNow(fragmentManager, null)
                R.id.remove -> appsRepo.updateAsync(removeHomeApp(homeAppIndex))
                R.id.move_up -> appsRepo.updateAsync(decrementHomeAppIndex(homeAppIndex))
                R.id.move_down -> appsRepo.updateAsync(incrementHomeAppIndex(apps[homeAppIndex].homeAppIndex))
            }
            true
        }
        menu.show()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.home_fragment_list_item_app_name)
    }
}
