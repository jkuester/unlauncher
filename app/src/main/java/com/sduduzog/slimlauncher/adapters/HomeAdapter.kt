package com.sduduzog.slimlauncher.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.models.HomeApp
import com.sduduzog.slimlauncher.ui.main.HomeFragment
import com.sduduzog.slimlauncher.utils.gravity

class HomeAdapter(
    private val listener: HomeFragment,
    private val unlauncherDataSource: UnlauncherDataSource
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private var apps: List<HomeApp> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps.elementAt(position)
        holder.mLabelView.text = item.appNickname ?: item.appName
        holder.mLabelView.setOnClickListener {
            listener.onLaunch(item, it)
        }
        unlauncherDataSource.corePreferencesRepo.liveData().observe(listener.viewLifecycleOwner) {
            holder.mLabelView.gravity = it.alignmentFormat.gravity()
        }
    }

    override fun getItemCount(): Int = apps.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: List<HomeApp>) {
        this.apps = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mLabelView: TextView = mView.findViewById(R.id.home_fragment_list_item_app_name)

        override fun toString(): String {
            return super.toString() + " '" + mLabelView.text + "'"
        }
    }
}
