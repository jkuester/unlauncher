package com.jkuester.unlauncher.android.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

abstract class AbstractClock(context: Context) : LinearLayout(context) {
    protected fun launchShowAlarms(fragment: Fragment) = OnClickListener {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            fragment.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    protected fun launchShowCalendar(fragment: Fragment) = OnClickListener {
        try {
            val calendarUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
            val intent = Intent(Intent.ACTION_VIEW, calendarUri)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            fragment.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}
