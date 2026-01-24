package com.jkuester.unlauncher.bindings

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.getCurrentDateString
import com.sduduzog.slimlauncher.databinding.ClockDigitalBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun getTimeFormat(context: Context, timeFormat: TimeFormat): java.text.DateFormat = when (timeFormat) {
    TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
    TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
    else -> DateFormat.getTimeFormat(context)
}

fun updateDigitalClockViews(
    context: Context,
    resources: Resources,
    corePrefsRepo: DataRepository<CorePreferences>,
    binding: ClockDigitalBinding
): () -> Unit = {
    binding.digitalTime.text = getTimeFormat(context, corePrefsRepo.get().timeFormat).format(Date())
    binding.digitalDate.text = getCurrentDateString(resources)
}

fun observeTimeFormatChanges(corePrefsRepo: DataRepository<CorePreferences>, onUpdate: () -> Unit) {
    var currentTimeFormat: TimeFormat? = null
    corePrefsRepo.observe { corePrefs ->
        val previousTimeFormat = currentTimeFormat
        currentTimeFormat = corePrefs.timeFormat
        if (previousTimeFormat != null && previousTimeFormat != currentTimeFormat) {
            onUpdate()
        }
    }
}
