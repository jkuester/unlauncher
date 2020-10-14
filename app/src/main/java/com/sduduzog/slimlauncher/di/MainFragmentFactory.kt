package com.sduduzog.slimlauncher.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.sduduzog.slimlauncher.models.MainViewModel
import com.sduduzog.slimlauncher.ui.main.HomeFragment
import javax.inject.Inject

class MainFragmentFactory @Inject constructor(private val mainViewModel: MainViewModel): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            HomeFragment::class.java -> HomeFragment(mainViewModel)
            else -> super.instantiate(classLoader, className)
        }
    }
}