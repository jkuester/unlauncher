package com.sduduzog.slimlauncher

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

inline fun <reified T : Fragment> launchFragmentInHiltContainer(
        fragmentArgs: Bundle? = null,
        @StyleRes themeResId: Int = R.style.AppTheme,
        fragmentFactory: FragmentFactory? = null,
        crossinline action: Fragment.() -> Unit = {}) {
    val startActivityIntent = Intent.makeMainActivity(
            ComponentName(
                    ApplicationProvider.getApplicationContext(),
                    HiltTestActivity::class.java
            )
    ).putExtra(FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, themeResId)

    ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        if (fragmentFactory !== null) {
            activity.supportFragmentManager.fragmentFactory = fragmentFactory
        }
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
                Preconditions.checkNotNull(T::class.java.classLoader),
                T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, fragment, "")
                .commitNow()

        fragment.action()
    }
}