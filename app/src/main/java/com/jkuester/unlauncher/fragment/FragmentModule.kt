package com.jkuester.unlauncher.fragment

import androidx.datastore.core.DataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WithFragmentLifecycle

// java.util.function.Supplier class not supported in our current minimum version so roll our own.
interface LifecycleOwnerSupplier {
    fun get(): LifecycleOwner
}

@Module
@InstallIn(FragmentComponent::class)
class FragmentModule {
    @Provides
    @FragmentScoped
    fun provideLifecycleScope(fragment: Fragment): CoroutineScope = fragment.lifecycleScope

    @Provides
    @FragmentScoped
    fun provideLifecycleOwner(fragment: Fragment) = object : LifecycleOwnerSupplier {
        override fun get(): LifecycleOwner = fragment.viewLifecycleOwner
    }

    @Provides
    @WithFragmentLifecycle
    @FragmentScoped
    fun provideCorePreferencesRepo(fragment: Fragment, prefsStore: DataStore<CorePreferences>) =
        CorePreferencesRepository(
            prefsStore,
            provideLifecycleScope(fragment),
            provideLifecycleOwner(fragment)
        )
}
