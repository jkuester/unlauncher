package com.sduduzog.slimlauncher

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    public fun getContext() : Context {
        return this
    }

    companion object {
        fun getContext(): Context {
            return this.getContext()
        }
    }
}