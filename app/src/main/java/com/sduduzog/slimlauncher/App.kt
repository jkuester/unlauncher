package com.sduduzog.slimlauncher

import android.app.Application
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    val isDefaultLauncher: Boolean
        get() {
            val intent = Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            val res = packageManager.resolveActivity(intent, 0)
            if (res?.activityInfo == null) {
                // should not happen. A home is always installed, isn't it?
                return false
            }
            return packageName == res.activityInfo?.packageName
        }
}

