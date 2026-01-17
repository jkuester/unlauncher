package com.jkuester.unlauncher.bindings

import android.content.res.Resources
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.AnalogClockType
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.databinding.ClockAnalogBinding

fun setupAnalogClockDateClickListener(fragment: Fragment) = { binding: ClockAnalogBinding ->
    binding.analogDate.setOnClickListener(launchShowCalendar(fragment))
}

fun updateAnalogClockDate(resources: Resources) = { binding: ClockAnalogBinding ->
    binding.analogDate.text = getCurrentDateString(resources)
}

private fun getTickCount(analogClockType: AnalogClockType) = when (analogClockType) {
    AnalogClockType.analog_0 -> 0
    AnalogClockType.analog_1 -> 1
    AnalogClockType.analog_2 -> 2
    AnalogClockType.analog_3 -> 3
    AnalogClockType.analog_4 -> 4
    AnalogClockType.analog_6 -> 6
    AnalogClockType.analog_12 -> 12
    AnalogClockType.analog_60 -> 60
    else -> 12
}

fun observeAnalogClockTypeChanges(
    corePrefsRepo: DataRepository<CorePreferences>,
    onInitialValue: (Int) -> Unit,
    onTickCountChanged: (Int) -> Unit
) {
    var currentTickCount: Int? = null
    corePrefsRepo.observe { corePrefs ->
        val previousTickCount = currentTickCount
        currentTickCount = getTickCount(corePrefs.analogClockType)
        if (previousTickCount == null) {
            onInitialValue(currentTickCount!!)
        } else if (previousTickCount != currentTickCount) {
            onTickCountChanged(currentTickCount!!)
        }
    }
}
