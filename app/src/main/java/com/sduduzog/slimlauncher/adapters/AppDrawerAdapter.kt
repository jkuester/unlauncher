package com.sduduzog.slimlauncher.adapters

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsRepository
import com.sduduzog.slimlauncher.ui.main.HomeFragment
import com.sduduzog.slimlauncher.utils.firstUppercase
import java.lang.Integer.min


class AppDrawerAdapter(
    private val listener: HomeFragment.AppDrawerListener,
    lifecycleOwner: LifecycleOwner,
    appsRepo: UnlauncherAppsRepository
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val regex = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/? ]")

        private var apps: List<UnlauncherApp> = listOf()
    private var groupedApps: Map<Char, List<UnlauncherApp>> = emptyMap()

    //    private var apps: List<UnlauncherApp> = listOf()
    private var filteredApps: Map<Char, List<UnlauncherApp>> = emptyMap()
    private var filterQuery = ""

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    init {
        appsRepo.liveData().observe(lifecycleOwner) { unlauncherApps ->
            apps = unlauncherApps.appsList.filter { app -> app.displayInDrawer }.toList()
            groupedApps =
                apps.groupBy { app -> app.displayName.firstUppercase() }.toSortedMap()
            updateDisplayedApps()
        }
    }

    override fun getItemCount(): Int = filteredApps.size + apps.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val items = filteredApps.values.flatten()
        if (isHeaderPosition(position)) {
            val item = items[position]
            (holder as HeaderViewHolder).bind(item)
        } else {
            val item = items[position - 1]
            (holder as ViewHolder).bind(item)
            holder.itemView.setOnClickListener {
                listener.onAppClicked(item)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (isHeaderPosition(position)) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.app_drawer_fragment_header_item, parent, false)
            HeaderViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_app_fragment_list_item, parent, false)
            ViewHolder(itemView)
        }
    }

    fun setAppFilter(query: String = "") {
        filterQuery = regex.replace(query, "")
        this.updateDisplayedApps()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDisplayedApps() {
        filteredApps = groupedApps.values.flatten().filter { app ->
            regex.replace(app.displayName, "").contains(filterQuery, ignoreCase = true)
        }.groupBy { app -> app.displayName.firstUppercase() }.toSortedMap()

        notifyDataSetChanged()
    }

    private fun isHeaderPosition(position: Int): Boolean {
        if (position == 0) {
            return true
        }
        val size = filteredApps.values.flatten().size
        val pos = minOf(size - 1, position + 1)
        val currentLetter = filteredApps.values.flatten()[position].displayName.firstUppercase()
        val previousLetter =
            filteredApps.values.flatten()[pos].displayName.firstUppercase()
        return currentLetter != previousLetter
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

        fun bind(item: UnlauncherApp) {
            appName.text = item.displayName
        }
    }

    inner class HeaderViewHolder(headerView: View) : RecyclerView.ViewHolder(headerView) {
        private val header: TextView = itemView.findViewById(R.id.aa_list_header_letter)

        override fun toString(): String {
            return super.toString() + " '${header.text}'"
        }

        fun bind(item: UnlauncherApp) {
            header.text = item.displayName.firstUppercase().toString()
        }
    }

    inner class StickyHeaderDecoration<B>(
//        val adapter: StickyHeaderAdaper<*>,
        root: View,
        @LayoutRes headerLayout: Int
    ) :
        ItemDecoration() {


        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val topChild = parent.getChildAt(0)
            val secondChild = parent.getChildAt(1)

            parent.getChildAdapterPosition(topChild).let { topPosition ->
//                val header = adapter.getHeaderForCurrentPosition(topPosition)
//                headerView.tvStickyHeader.text = header

                layoutHeaderView(topChild)
//                canvas.drawHeaderView(topChild, secondChild)
            }
        }

        private fun layoutHeaderView(topView: View) {
//            headerView.measure(
//                MeasureSpec.makeMeasureSpec(topView.width, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
//            )
//            headerView.layout(topView.left, 0, topView.right, headerView.measuredHeight)
        }
    }
}