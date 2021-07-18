package com.sduduzog.slimlauncher.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.utils.OnAppClickedListener

class CustomizeAppDrawerAppsAdapter(private val listener: OnAppClickedListener) :
    RecyclerView.Adapter<CustomizeAppDrawerAppsAdapter.ViewHolder>() {

    private var apps: List<App> = listOf()
    private var hiddenApps: List<App> = listOf()

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        holder.appName.text = item.appName
        holder.appName.isChecked = !hiddenApps.contains(item)
        holder.itemView.setOnClickListener {
            listener.onAppClicked(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.customize_app_drawer_fragment_app_list_item, parent, false)
        return ViewHolder(view)
    }

    fun setItems(apps: List<App>, hiddenApps: List<App>) {
        this.apps = apps
        this.hiddenApps = hiddenApps
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appName: CheckBox =
            itemView.findViewById(R.id.customize_app_drawer_fragment_app_list_item)

        override fun toString(): String {
            return super.toString() + " '${appName.text}'"
        }
    }
}