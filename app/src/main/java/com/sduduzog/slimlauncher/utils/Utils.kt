package com.sduduzog.slimlauncher.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.DisplayMetrics
import android.view.WindowInsets
import androidx.annotation.StringRes
import com.sduduzog.slimlauncher.R


private fun isAppDefaultLauncher(context: Context?): Boolean {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    val res = context?.packageManager?.resolveActivity(intent, 0)
    if (res?.activityInfo == null) {
        // should not happen. A home is always installed, isn't it?
        return false
    }
    return context.packageName == res.activityInfo?.packageName
}

private fun intentContainsDefaultLauncher(intent: Intent?): Boolean = intent?.action == Intent.ACTION_MAIN && intent.categories?.contains(Intent.CATEGORY_HOME) == true

fun isActivityDefaultLauncher(activity: Activity?): Boolean = isAppDefaultLauncher(activity) || intentContainsDefaultLauncher(activity?.intent)

fun getScreenWidth(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val bounds: Rect = windowMetrics.bounds
        val insets: Insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.systemBars()
        )
        if (activity.resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE
            && activity.resources.configuration.smallestScreenWidthDp < 600
        ) { // landscape and phone
            val navigationBarSize: Int = insets.right + insets.left
            bounds.width() - navigationBarSize
        } else { // portrait or tablet
            bounds.width()
        }
    } else {
        val outMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(outMetrics)
        outMetrics.widthPixels
    }
}

fun getScreenHeight(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val bounds: Rect = windowMetrics.bounds
        val insets: Insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.systemBars()
        )
        if (activity.resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE
            && activity.resources.configuration.smallestScreenWidthDp < 600
        ) { // landscape and phone
            bounds.height()
        } else { // portrait or tablet
            val navigationBarSize: Int = insets.bottom
            bounds.height() - navigationBarSize
        }
    } else {
        val outMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(outMetrics)
        outMetrics.heightPixels
    }
}

fun createTitleAndSubtitleText(context: Context, @StringRes titleRes: Int, @StringRes subtitleRes: Int) : CharSequence
    = createTitleAndSubtitleText(context, context.getString(titleRes), context.getString(subtitleRes))

fun createTitleAndSubtitleText(context: Context, title: CharSequence, subtitle: CharSequence) : CharSequence {
    val spanBuilder = SpannableStringBuilder("$title\n$subtitle")
    spanBuilder.setSpan(TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Large),
        0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    spanBuilder.setSpan(
        TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Small),
        title.length + 1,
        title.length + 1 + subtitle.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spanBuilder
}

fun String.firstUppercase() = this.first().uppercase()