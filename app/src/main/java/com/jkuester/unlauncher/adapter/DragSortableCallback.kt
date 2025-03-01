package com.jkuester.unlauncher.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

interface DragSortable<T : RecyclerView.ViewHolder> {
    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onRowSelected(viewHolder: T)
    fun onRowClear(viewHolder: T)
    fun asTargetViewHolder(viewHolder: RecyclerView.ViewHolder?): T?
}

class DragSortableCallback<T : RecyclerView.ViewHolder>(private val adapter: DragSortable<T>) :
    ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled() = false
    override fun isItemViewSwipeEnabled() = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onRowMoved(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val target = adapter.asTargetViewHolder(viewHolder)
        if (target != null && actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            adapter.onRowSelected(target)
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val target = adapter.asTargetViewHolder(viewHolder)
        if (target != null) {
            adapter.onRowClear(target)
        }
    }
}
