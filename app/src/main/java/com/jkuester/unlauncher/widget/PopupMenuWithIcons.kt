package com.jkuester.unlauncher.widget

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.PopupMenu

class PopupMenuWithIcons(context: Context, anchor: View) : PopupMenu(context, anchor) {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.setForceShowIcon(true)
        }
    }
}
