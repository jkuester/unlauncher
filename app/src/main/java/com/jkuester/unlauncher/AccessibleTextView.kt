package com.jkuester.unlauncher

import android.content.Context
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView

class AccessibleTextView(context: Context) : AppCompatTextView(context) {
    var downOperation: (() -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return false
    }

    override fun performClick(): Boolean {
        super.performClick()
        doSomething()
        return true
    }

    private fun doSomething() {
        Toast.makeText(context, "did something", Toast.LENGTH_SHORT).show()
    }
}
