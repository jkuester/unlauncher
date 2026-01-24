package com.jkuester.unlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.view.View
import com.jkuester.unlauncher.android.view.createAnalogClockView
import com.jkuester.unlauncher.android.view.createBinaryClockView
import com.jkuester.unlauncher.android.view.createDigitalClockView
import com.jkuester.unlauncher.datastore.proto.ClockType
import com.sduduzog.slimlauncher.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentDateString(resources: Resources): String = SimpleDateFormat(
    resources.getString(R.string.main_date_format),
    Locale.getDefault()
).format(Date())

fun createNewClock(ctx: Context, clockType: ClockType): View = when (clockType) {
    ClockType.analog -> createAnalogClockView(ctx)
    ClockType.binary -> createBinaryClockView(ctx)
    else -> createDigitalClockView(ctx)
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
