package com.sduduzog.slimlauncher.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

open class HomeWatcher(internal val context: Context) {

    private var listener: OnHomePressedListener? = null
    internal var receiver: InnerReceiver? = null
    internal val filter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)

    companion object {
        fun createInstance(context: Context): HomeWatcher {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return Sv2HomeWatcher(context)
            }
            return HomeWatcher(context)
        }
    }

    fun setOnHomePressedListener(listener: OnHomePressedListener) {
        this.listener = listener
        receiver = InnerReceiver()
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    open fun startWatch() {
        receiver?.let {
            context.registerReceiver(it, filter, Context.RECEIVER_NOT_EXPORTED)
        }
    }

    fun stopWatch() {
        receiver?.let {
            context.unregisterReceiver(it)
        }
    }

    inner class InnerReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action != Intent.ACTION_CLOSE_SYSTEM_DIALOGS) return
            val reason = intent.getStringExtra("reason") ?: return
            if (reason != "homekey") return
            listener?.onHomePressed()
        }
    }

    interface OnHomePressedListener {
        fun onHomePressed()
    }

    private class Sv2HomeWatcher(context: Context): HomeWatcher(context) {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @TargetApi(Build.VERSION_CODES.S_V2)
        override fun startWatch() {
            receiver?.let {
                context.registerReceiver(it, filter)
            }
        }
    }
}