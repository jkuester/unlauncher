package com.jkuester.unlauncher.fragment

import android.view.LayoutInflater
import androidx.datastore.core.DataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.DataRepositoryImpl
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
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
interface Supplier<T> {
    fun get(): T
}

@Module
@InstallIn(FragmentComponent::class)
class FragmentModule {
    @Provides
    @FragmentScoped
    fun provideFragmentManager(fragment: Fragment) = fragment.childFragmentManager

    @Provides
    @FragmentScoped
    fun provideLayoutInflaterSupplier(fragment: Fragment) = object : Supplier<LayoutInflater> {
        override fun get(): LayoutInflater = fragment.layoutInflater
    }

    @Provides
    @FragmentScoped
    fun provideLifecycleScope(fragment: Fragment): CoroutineScope = fragment.lifecycleScope

    @Provides
    @FragmentScoped
    fun provideLifecycleOwnerSupplier(fragment: Fragment) = object : Supplier<LifecycleOwner> {
        override fun get(): LifecycleOwner = fragment.viewLifecycleOwner
    }

    @Provides @WithFragmentLifecycle
    @FragmentScoped
    fun provideCorePreferencesRepo(
        prefsStore: DataStore<CorePreferences>,
        lifecycleScope: CoroutineScope,
        lifecycleOwnerSupplier: Supplier<LifecycleOwner>
    ): DataRepository<CorePreferences> = DataRepositoryImpl(
        prefsStore,
        lifecycleScope,
        lifecycleOwnerSupplier,
        CorePreferences::getDefaultInstance
    )

    @Provides
    @FragmentScoped
    fun provideQuickButtonPreferencesRepo(
        prefsStore: DataStore<QuickButtonPreferences>,
        lifecycleScope: CoroutineScope,
        lifecycleOwnerSupplier: Supplier<LifecycleOwner>
    ): DataRepository<QuickButtonPreferences> = DataRepositoryImpl(
        prefsStore,
        lifecycleScope,
        lifecycleOwnerSupplier,
        QuickButtonPreferences::getDefaultInstance
    )

    @Provides
    @FragmentScoped
    fun provideUnlauncherAppsRepo(
        prefsStore: DataStore<UnlauncherApps>,
        lifecycleScope: CoroutineScope,
        lifecycleOwnerSupplier: Supplier<LifecycleOwner>
    ): DataRepository<UnlauncherApps> = DataRepositoryImpl(
        prefsStore,
        lifecycleScope,
        lifecycleOwnerSupplier,
        UnlauncherApps::getDefaultInstance
    )
}
