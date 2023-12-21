package com.sduduzog.slimlauncher.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.models.HomeApp
import com.sduduzog.slimlauncher.utils.OnLaunchAppListener

/**
 * Corresponding to the resulting gravity, not the option key
 */
enum class Alignment (val value: Int) {
    LEFT(3),
    RIGHT(5),
    CENTER(1)
}

class HomeAdapter(private val listener: OnLaunchAppListener)
    : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private var apps: List<HomeApp> = listOf()
    private var gravity: Alignment = Alignment.LEFT

    constructor(listener: OnLaunchAppListener, alignment: Int) : this(listener) {
        setAlignment(alignment)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.main_fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps.elementAt(position)
        holder.mLabelView.text = item.appNickname ?: item.appName
        holder.mLabelView.gravity = gravity.value
        holder.mLabelView.setOnClickListener {
            listener.onLaunch(item, it)
        }
    }

    override fun getItemCount(): Int = apps.size

    fun setItems(list: List<HomeApp>) {
        this.apps = list
        notifyDataSetChanged()
    }

    fun getGravity(): Alignment = gravity

    fun setGravity(gravity: Alignment) {
        this.gravity = gravity
    }

    private fun setAlignment(alignment: Int) {
        gravity = when (alignment) {
            2 -> Alignment.RIGHT
            1 -> Alignment.CENTER
            else -> Alignment.LEFT
        }
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mLabelView: TextView = mView.findViewById(R.id.home_fragment_list_item_app_name)

        override fun toString(): String {
            return super.toString() + " '" + mLabelView.text + "'"
        }
    }
}