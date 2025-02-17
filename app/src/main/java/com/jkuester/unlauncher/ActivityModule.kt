package com.jkuester.unlauncher

import android.app.Activity
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.fragment.LifecycleOwnerSupplier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WithActivityLifecycle

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {
    @Provides
    @ActivityScoped
    fun provideComponentActivity(activity: Activity): ComponentActivity = activity as ComponentActivity

    @Provides
    @ActivityScoped
    fun provideResources(activity: ComponentActivity): Resources = activity.resources

    @Provides @WithActivityLifecycle
    @ActivityScoped
    fun provideCorePreferencesRepo(activity: ComponentActivity, prefsStore: DataStore<CorePreferences>) =
        CorePreferencesRepository(
            prefsStore,
            activity.lifecycleScope,
            object : LifecycleOwnerSupplier {
                override fun get(): LifecycleOwner = activity
            }
        )
}
