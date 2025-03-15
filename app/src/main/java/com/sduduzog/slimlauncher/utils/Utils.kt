package com.sduduzog.slimlauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.sduduzog.slimlauncher.R

fun isDefaultLauncher(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN)
        .apply { addCategory(Intent.CATEGORY_HOME) }
    val res = context.packageManager?.resolveActivity(intent, 0)
    return context.packageName == res?.activityInfo?.packageName
}

fun createTitleAndSubtitleText(context: Context, title: CharSequence, subtitle: CharSequence): CharSequence {
    val spanBuilder = SpannableStringBuilder("$title\n$subtitle")
    spanBuilder.setSpan(
        TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Large),
        0,
        title.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spanBuilder.setSpan(
        TextAppearanceSpan(context, R.style.TextAppearance_AppCompat_Small),
        title.length + 1,
        title.length + 1 + subtitle.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spanBuilder
}

fun String.firstUppercase() = this.first().uppercase()

fun ApplicationInfo.isSystemApp(): Boolean = (this.flags and ApplicationInfo.FLAG_SYSTEM != 0) ||
    (this.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)

fun AlignmentFormat.gravity(): Int = when (this.number) {
    2 -> 5 // RIGHT
    1 -> 1 // CENTER
    else -> 3 // LEFT
}
