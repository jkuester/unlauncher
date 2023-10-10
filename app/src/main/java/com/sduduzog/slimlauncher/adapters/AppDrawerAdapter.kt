package com.sduduzog.slimlauncher.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.ui.main.HomeFragment

class AppDrawerAdapter(
    private val listener: HomeFragment.AppDrawerListener,
    lifecycleOwner: LifecycleOwner,
    appsRepo: UnlauncherAppsRepository
) : RecyclerView.Adapter<AppDrawerAdapter.ViewHolder>() {
    private val regex = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/? ]")
    private var apps: List<UnlauncherApp> = listOf()
    private var filteredApps: List<UnlauncherApp> = listOf()
    private var filterQuery = ""

    init {
        appsRepo.liveData().observe(lifecycleOwner, { unlauncherApps ->
            apps = unlauncherApps.appsList.filter { app -> app.displayInDrawer }.toList()
            updateDisplayedApps()
        })
    }

    override fun getItemCount(): Int = filteredApps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredApps[position]
        holder.appName.text = item.displayName
        holder.itemView.setOnClickListener {
            listener.onAppClicked(item)
        }
        holder.itemView.setOnLongClickListener {
            listener.onAppLongClicked(item, it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_app_fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    fun setAppFilter(query: String = "") {
        filterQuery = regex.replace(query, "")
        this.updateDisplayedApps()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDisplayedApps() {
        filteredApps = apps.filter { app ->
            regex.replace(app.displayName, "").contains(filterQuery, ignoreCase = true)
        }.toList()
        notifyDataSetChanged()
    }

    val searchBoxListener: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Do nothing
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            setAppFilter(s.toString())
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val appName: TextView = itemView.findViewById(R.id.aa_list_item_app_name)

        override fun toString(): String {
            return super.toString() + " '${appName.text}'"
        }
    }
}