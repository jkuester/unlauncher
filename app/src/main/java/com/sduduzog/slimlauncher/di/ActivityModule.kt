package com.sduduzog.slimlauncher.di

import android.app.Activity
import androidx.core.app.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @Provides
    fun provideLifecycleCoroutineScope(activity: Activity): LifecycleCoroutineScope =
        (activity as ComponentActivity).lifecycleScope

    @Provides
    @ActivityScoped
    fun providesUnlauncherDataSource(
        activity: Activity,
        lifecycleCoroutineScope: LifecycleCoroutineScope
    ) = UnlauncherDataSource(activity, lifecycleCoroutineScope)
}