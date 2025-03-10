package com.jkuester.unlauncher

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.PopupMenu

fun createPopupMenuWithIcons(context: Context, anchor: View): PopupMenu {
    val menu = PopupMenu(context, anchor)
    if (androidSdkAtLeast(Build.VERSION_CODES.Q)) {
        menu.setForceShowIcon(true)
    }
    return menu
}
