package com.jkuester.unlauncher.android.view

import android.content.Context
import android.view.Gravity
import androidx.fragment.app.Fragment
import com.jkuester.unlauncher.bindings.observeTimeFormatChanges
import com.jkuester.unlauncher.bindings.updateDigitalClockViews
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.ClockDigitalBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import javax.inject.Inject

fun createDigitalClockView(ctx: Context): DigitalClockView = DigitalClockView(ctx)

@AndroidEntryPoint
@WithFragmentBindings
class DigitalClockView(context: Context) : AbstractClock(context) {
    @Inject
    lateinit var fragment: Fragment

    @Inject
    @WithFragmentLifecycle
    lateinit var corePrefsRepo: DataRepository<CorePreferences>

    private val binding: ClockDigitalBinding
    private val updateChildViews: () -> Unit

    init {
        inflate(context, R.layout.clock_digital, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ClockDigitalBinding
            .bind(this)
            .also { b ->
                b.digitalTime.setOnClickListener(launchShowAlarms(fragment))
                b.digitalDate.setOnClickListener(launchShowCalendar(fragment))
            }
        updateChildViews = updateDigitalClockViews(context, resources, corePrefsRepo, binding)
        observeTimeFormatChanges(corePrefsRepo, this::invalidate)
        updateChildViews()
    }

    override fun invalidate() {
        super.invalidate()
        updateChildViews()
    }
}
