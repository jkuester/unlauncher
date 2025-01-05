package com.sduduzog.slimlauncher

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@TargetApi(Build.VERSION_CODES.O)
class AddShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        var request = launcherApps.getPinItemRequest(getIntent())
        if (request == null) {
            this.finish()
            return
        }

        if (request.getRequestType() == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
            // refreshApps() // FIXME: need to set signal...
            request.accept()
            this.finish()
            return
        }
    }
}
