package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.databinding.ClockBinaryBinding

fun setupBinaryClockDateClickListener(fragment: Fragment) = { binding: ClockBinaryBinding ->
    binding.binaryDate.setOnClickListener(launchShowCalendar(fragment))
}

fun updateBinaryClockDate(resources: Resources) = { binding: ClockBinaryBinding ->
    binding.binaryDate.text = getCurrentDateString(resources)
}

private fun getIs24HourFormat(context: Context, timeFormat: TimeFormat): Boolean = when (timeFormat) {
    TimeFormat.twenty_four_hour -> true
    TimeFormat.twelve_hour -> false
    else -> DateFormat.is24HourFormat(context)
}

fun observeIs24HourFormatChanges(
    context: Context,
    corePrefsRepo: DataRepository<CorePreferences>,
    onInitialValue: (Boolean) -> Unit,
    onFormatChanged: (Boolean) -> Unit
) {
    var currentIs24Hour: Boolean? = null
    corePrefsRepo.observe { corePrefs ->
        val previousIs24Hour = currentIs24Hour
        currentIs24Hour = getIs24HourFormat(context, corePrefs.timeFormat)
        if (previousIs24Hour == null) {
            onInitialValue(currentIs24Hour!!)
        } else if (previousIs24Hour != currentIs24Hour) {
            onFormatChanged(currentIs24Hour!!)
        }
    }
}
