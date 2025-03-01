package com.jkuester.unlauncher.view

import android.content.Context

class TouchableTextView(context: Context) : androidx.appcompat.widget.AppCompatTextView(context) {
    override fun performClick(): Boolean = super.performClick()
}
