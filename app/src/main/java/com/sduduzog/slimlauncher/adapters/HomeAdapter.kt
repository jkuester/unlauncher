package com.sduduzog.slimlauncher.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.ui.main.HomeFragment
import com.sduduzog.slimlauncher.utils.gravity

class HomeAdapter(
    private val listener: HomeFragment,
    private val corePreferencesRepo: DataRepository<CorePreferences>
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private var apps: List<UnlauncherApp> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps.elementAt(position)
        holder.mLabelView.text = item.displayName
        holder.mLabelView.setOnClickListener {
            listener.onLaunch(item, it)
        }
        corePreferencesRepo.observe {
            holder.mLabelView.gravity = it.alignmentFormat.gravity()
        }
    }

    override fun getItemCount(): Int = apps.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: List<UnlauncherApp>) {
        this.apps = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mLabelView: TextView = mView.findViewById(R.id.home_fragment_list_item_app_name)

        override fun toString(): String = super.toString() + " '" + mLabelView.text + "'"
    }
}
