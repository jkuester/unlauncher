package com.jkuester.unlauncher.view

import android.content.Context
import android.text.format.DateFormat
import android.view.Gravity
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.TimeFormat
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.jkuester.unlauncher.getCurrentDateString
import com.jkuester.unlauncher.launchShowAlarms
import com.jkuester.unlauncher.launchShowCalendar
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockDigitalBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
@WithFragmentBindings
class DigitalClockView(context: Context) : LinearLayout(context) {
    @Inject
    lateinit var fragment: Fragment
    @Inject @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    private val binding: ClockDigitalBinding
    private var timeFormat: TimeFormat

    init {
        inflate(context, R.layout.clock_digital, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ClockDigitalBinding.bind(this)
        timeFormat = corePrefsRepo.get().timeFormat
        corePrefsRepo.observe(this::listenForChangesToTimeFormat)
        binding.digitalTime.setOnClickListener(launchShowAlarms(fragment))
        binding.digitalDate.setOnClickListener(launchShowCalendar(fragment))

        updateChildViews()
    }

    override fun invalidate() {
        super.invalidate()
        updateChildViews()
    }

    private fun listenForChangesToTimeFormat(corePrefs: CorePreferences) {
        val originalTimeFormat = timeFormat
        timeFormat = corePrefs.timeFormat
        if (originalTimeFormat != timeFormat) {
            invalidate()
        }
    }

    private fun updateChildViews() {
        val timeStringFormat = when (timeFormat) {
            TimeFormat.twenty_four_hour -> SimpleDateFormat("H:mm", Locale.getDefault())
            TimeFormat.twelve_hour -> SimpleDateFormat("h:mm aa", Locale.getDefault())
            else -> DateFormat.getTimeFormat(context)
        }
        binding.digitalTime.text = timeStringFormat.format(Date())
        binding.digitalDate.text = getCurrentDateString(resources)
    }
}
