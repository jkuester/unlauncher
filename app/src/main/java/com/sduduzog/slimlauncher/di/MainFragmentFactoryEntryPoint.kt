package com.sduduzog.slimlauncher.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface MainFragmentFactoryEntryPoint {
    fun getMainFragmentFactory(): MainFragmentFactory
}