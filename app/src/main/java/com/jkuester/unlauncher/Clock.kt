package com.jkuester.unlauncher

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.jkuester.unlauncher.view.AnalogClockView
import com.jkuester.unlauncher.view.BinaryClockView
import com.jkuester.unlauncher.view.DigitalClockView
import com.sduduzog.slimlauncher.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun launchShowAlarms(fragment: Fragment) = OnClickListener {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        fragment.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun launchShowCalendar(fragment: Fragment) = OnClickListener {
    try {
        val calendarUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
        val intent = Intent(Intent.ACTION_VIEW, calendarUri)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        fragment.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun getCurrentDateString(resources: Resources): String = SimpleDateFormat(
    resources.getString(R.string.main_date_format),
    Locale.getDefault()
).format(Date())

fun createNewClock(ctx: Context, clockType: ClockType): View = when (clockType) {
    ClockType.analog -> AnalogClockView(ctx)
    ClockType.binary -> BinaryClockView(ctx)
    else -> DigitalClockView(ctx)
}

class ClockReceiver : BroadcastReceiver() {
    var clock: View? = null

    override fun onReceive(ctx: Context?, intent: Intent?) {
        clock?.requestLayout()
        clock?.invalidate()
    }

    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_TIME_TICK))
        this.onReceive(context, null)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}
