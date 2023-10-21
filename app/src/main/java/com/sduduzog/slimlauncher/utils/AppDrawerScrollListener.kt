package com.sduduzog.slimlauncher.utils

import androidx.recyclerview.widget.RecyclerView

abstract class AppDrawerScrollListener : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
            if (dy > 0) {
                // finger moves upwards
                onScrolledDown()
            } else if (dy < 0) {
                // finger moves downwards
                onScrolledUp()
            }
        }
    }

    /**
     * Called when user is scrolling upward the [RecyclerView]
     */
    abstract fun onScrolledUp()

    /**
     * * Called when user is scrolling downward the [RecyclerView]
     */
    abstract fun onScrolledDown()


}