package com.jkuester.unlauncher.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.getHomeApps
import com.jkuester.unlauncher.datasource.setHomeApps
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.view.TouchableTextView
import com.sduduzog.slimlauncher.R
import java.util.Collections

class CustomizeHomeAppsListAdapter(
    private val appsRepo: DataRepository<UnlauncherApps>,
    private val dragHandleListener: (ViewHolder) -> Boolean
) : RecyclerView.Adapter<CustomizeHomeAppsListAdapter.ViewHolder>(),
    DragSortable<CustomizeHomeAppsListAdapter.ViewHolder> {
    private var apps: List<UnlauncherApp> = appsRepo.get().let(::getHomeApps)

    init {
        appsRepo.observe {
            apps = getHomeApps(it)
            // TODO Notify that the data set has changed
//            notifyDataSetChanged()
//            notifyItemChanged(int),
            //            notifyItemInserted(int),
            //            notifyItemRemoved(int),
            //            notifyItemRangeChanged(int, int),
            //            notifyItemRangeInserted(int, int),
            //            notifyItemRangeRemoved(int, int
        }
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.appName.text = item.displayName
        holder.appName.setOnClickListener {
        }

        // TODO drag and drop is bad for accessibility.
        // I think we should change to an arrow-based approach. The main question is how to trigger it.
        // - Long press somewhere?
        // - kebab menu on list item?
        // - kebab menu on the top right?
        // Honestly we could start super simple and just add "Move up", "Move down" buttons to the kebab menu.
        // Even better, remove the kebab menu. The whole row just becomes a button that opens the context menu to:
        // - Move up
        // - Move down
        // - Remove
        // - Rename
        // Another interesting iteration might be to put a kebab menu on the row with the Add button
        holder.dragHandle.setOnTouchListener { view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> dragHandleListener(holder)
                else -> view.performClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LayoutInflater
        .from(parent.context)
        .inflate(R.layout.customize_home_apps_list_item, parent, false)
        .let { view -> ViewHolder(view) }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val updatedApps = apps.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(updatedApps, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(updatedApps, i, i - 1)
            }
        }

        apps = updatedApps.toList()
        appsRepo.updateAsync(setHomeApps(apps))
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(viewHolder: CustomizeHomeAppsListAdapter.ViewHolder) {
        viewHolder.wrapper.setBackgroundColor(Color.GRAY)
    }

    override fun onRowClear(viewHolder: CustomizeHomeAppsListAdapter.ViewHolder) {
        viewHolder.wrapper.setBackgroundColor(Color.WHITE)
    }

    override fun asTargetViewHolder(viewHolder: RecyclerView.ViewHolder?): ViewHolder? =
        viewHolder as? CustomizeHomeAppsListAdapter.ViewHolder

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wrapper: ConstraintLayout = itemView.findViewById(R.id.list_item_wrapper)
        val dragHandle: TouchableTextView = itemView.findViewById(R.id.drag_handle)
        val appName: TextView = itemView.findViewById(R.id.app_name)
        val menuIcon: ImageView = itemView.findViewById(R.id.menu_icon)
    }
}
