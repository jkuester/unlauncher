package com.sduduzog.slimlauncher.utils

import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.sduduzog.slimlauncher.MainActivity
import com.sduduzog.slimlauncher.R

abstract class BaseFragment : Fragment(), ISubscriber {

    abstract fun getFragmentView(): ViewGroup


    override fun onResume() {
        super.onResume()
        val settings = requireContext().getSharedPreferences(getString(R.string.prefs_settings), AppCompatActivity.MODE_PRIVATE)
        val active = settings.getInt(getString(R.string.prefs_settings_key_theme), 0)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (active) {
                0, 3, 5 -> {
                    val flags = requireActivity().window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    getFragmentView().systemUiVisibility = flags
                }

            }
            val value = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.colorPrimary, value, true)
            requireActivity().window.statusBarColor = value.data
        }

    }

    override fun onStart() {
        super.onStart()
        with(activity as IPublisher) {
            this.attachSubscriber(this@BaseFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        with(activity as IPublisher) {
            this.detachSubscriber(this@BaseFragment)
        }
    }

    protected fun launchActivity(view: View, intent: Intent) {
        val left = 0
        val top = 0
        val width = view.measuredWidth
        val height = view.measuredHeight
        val opts = ActivityOptionsCompat.makeClipRevealAnimation(view, left, top, width, height)
        startActivity(intent, opts.toBundle())
    }

    open fun onBack(): Boolean = false

    open fun onHome() {}
}
