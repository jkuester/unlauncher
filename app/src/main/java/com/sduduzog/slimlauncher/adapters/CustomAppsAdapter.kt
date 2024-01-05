package com.sduduzog.slimlauncher.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.models.HomeApp
import com.sduduzog.slimlauncher.utils.OnItemActionListener
import com.sduduzog.slimlauncher.utils.OnShitDoneToAppsListener

class CustomAppsAdapter(private val listener: OnShitDoneToAppsListener) : RecyclerView.Adapter<CustomAppsAdapter.ViewHolder>(), OnItemActionListener {

    private var apps: MutableList<HomeApp> = mutableListOf()
    private lateinit var touchHelper: ItemTouchHelper

    override fun getItemCount(): Int = apps.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.customise_apps_fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.appName.text = item.appNickname ?: item.appName
        holder.dragHandle.setOnTouchListener { _, motionEvent ->
            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }
        holder.menuIcon.setOnClickListener {
            listener.onAppMenuClicked(it, item)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(apps: List<HomeApp>) {
        this.apps = sanitiseIndexes(apps) as MutableList<HomeApp>
        notifyDataSetChanged()
    }

    private fun sanitiseIndexes(apps: List<HomeApp>): List<HomeApp> {
        for (i in apps.indices) {
            apps[i].sortingIndex = i
        }
        return apps
    }

    fun setItemTouchHelper(touchHelper: ItemTouchHelper) {
        this.touchHelper = touchHelper
    }

    override fun onViewMoved(oldPosition: Int, newPosition: Int): Boolean {
        if ((oldPosition < apps.size) and (newPosition < apps.size)) {
            val app1 = apps[oldPosition]
            val app2 = apps[newPosition]
            app1.sortingIndex = newPosition
            app2.sortingIndex = oldPosition

            apps[oldPosition] = app2
            apps[newPosition] = app1
            notifyItemMoved(oldPosition, newPosition)
            return true
        }
        return false
    }


    override fun onViewIdle() {
        listener.onAppsUpdated(apps)
    }

    override fun onViewSwiped(position: Int) {
        // Do nothing. We are under attack!
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dragHandle: TextView = itemView.findViewById(R.id.ca_list_item_drag_handle)
        val appName: TextView = itemView.findViewById(R.id.ca_list_item_app_name)
        val menuIcon: ImageView = itemView.findViewById(R.id.ca_list_item_more_icon)

        override fun toString(): String {
            return super.toString() + " '${appName.text}'"
        }
    }
}