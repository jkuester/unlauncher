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

fun getCurrentDateString(resources: Resources) = SimpleDateFormat(
    resources.getString(R.string.main_date_format),
    Locale.getDefault()
).format(Date())

fun createNewClock(ctx: Context, clockType: ClockType): View = when (clockType) {
    ClockType.analog_0,
    ClockType.analog_1,
    ClockType.analog_2,
    ClockType.analog_3,
    ClockType.analog_4,
    ClockType.analog_6,
    ClockType.analog_12,
    ClockType.analog_60 -> {
        AnalogClockView(ctx)
    }
    else -> {
        DigitalClockView(ctx)
    }
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
