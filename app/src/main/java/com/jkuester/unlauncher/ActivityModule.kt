package com.jkuester.unlauncher

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @Provides
    fun provideLifecycleCoroutineScope(activity: Activity): CoroutineScope =
        (activity as ComponentActivity).lifecycleScope
}
