package com.jkuester.unlauncher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.decrementHomeAppIndex
import com.jkuester.unlauncher.datasource.getHomeApps
import com.jkuester.unlauncher.datasource.incrementHomeAppIndex
import com.jkuester.unlauncher.datasource.removeHomeApp
import com.jkuester.unlauncher.datasource.unlauncherAppMatches
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.dialog.RenameAppDisplayNameDialog
import com.jkuester.unlauncher.widget.PopupMenuWithIcons
import com.sduduzog.slimlauncher.R

class CustomizeHomeAppsListAdapter(
    private val appsRepo: DataRepository<UnlauncherApps>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<CustomizeHomeAppsListAdapter.ViewHolder>() {
    private var apps: List<UnlauncherApp> = appsRepo.get().let(::getHomeApps)
    private val notificationQueue = mutableListOf<() -> Unit>()

    init {
        appsRepo.observe { updatedApps ->
            apps = getHomeApps(updatedApps)
            notificationQueue.forEach { it.invoke() }
            notificationQueue.clear()
        }
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.name.text = item.displayName
        holder.name.setOnClickListener {
            val popupMenu = PopupMenuWithIcons(it.context, it)
            popupMenu.inflate(R.menu.customize_home_apps_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                val homeAppIndex = apps
                    .first(unlauncherAppMatches(item))
                    .homeAppIndex
                when (menuItem.itemId) {
                    R.id.rename -> rename(homeAppIndex)
                    R.id.remove -> removeAt(homeAppIndex)
                    R.id.move_up -> moveUp(homeAppIndex)
                    R.id.move_down -> moveDown(homeAppIndex)
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun rename(homeAppIndex: Int) {
        notificationQueue.add { notifyItemChanged(homeAppIndex) }
        RenameAppDisplayNameDialog(apps[homeAppIndex]).showNow(fragmentManager, null)
    }

    private fun removeAt(homeAppIndex: Int) {
        notificationQueue.add { notifyItemRemoved(homeAppIndex) }
        appsRepo.updateAsync(removeHomeApp(homeAppIndex))
    }

    private fun moveUp(homeAppIndex: Int) {
        if (homeAppIndex == 0) {
            return
        }
        notificationQueue.add { notifyItemMoved(homeAppIndex, homeAppIndex - 1) }
        appsRepo.updateAsync(decrementHomeAppIndex(homeAppIndex))
    }

    private fun moveDown(homeAppIndex: Int) {
        if (homeAppIndex == apps.size - 1) {
            return
        }
        notificationQueue.add { notifyItemMoved(homeAppIndex, homeAppIndex + 1) }
        appsRepo.updateAsync(incrementHomeAppIndex(apps[homeAppIndex].homeAppIndex))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater
        .from(parent.context)
        .inflate(R.layout.main_fragment_list_item, parent, false)
        .let { view -> ViewHolder(view) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.home_fragment_list_item_app_name)
    }
}
