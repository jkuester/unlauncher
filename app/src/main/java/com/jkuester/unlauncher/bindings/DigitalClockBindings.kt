package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowAlarms
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.databinding.ClockDigitalBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun setupDigitalClockClickListeners(fragment: Fragment) = { binding: ClockDigitalBinding ->
    binding.digitalTime.setOnClickListener(launchShowAlarms(fragment))
    binding.digitalDate.setOnClickListener(launchShowCalendar(fragment))
}

private fun getTimeFormat(context: Context, timeFormat: TimeFormat): java.text.DateFormat = when (timeFormat) {
    TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
    TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
    else -> DateFormat.getTimeFormat(context)
}

fun updateDigitalClockViews(context: Context, resources: Resources, timeFormat: TimeFormat) =
    { binding: ClockDigitalBinding ->
        binding.digitalTime.text = getTimeFormat(context, timeFormat).format(Date())
        binding.digitalDate.text = getCurrentDateString(resources)
    }

fun observeTimeFormatChanges(
    corePrefsRepo: DataRepository<CorePreferences>,
    onUpdate: () -> Unit
) {
    var currentTimeFormat: TimeFormat? = null
    corePrefsRepo.observe { corePrefs ->
        val previousTimeFormat = currentTimeFormat
        currentTimeFormat = corePrefs.timeFormat
        if (previousTimeFormat != null && previousTimeFormat != currentTimeFormat) {
            onUpdate()
        }
    }
}
