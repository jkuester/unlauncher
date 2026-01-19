package com.jkuester.unlauncher

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import com.jkuester.unlauncher.android.createPaint

fun getThemeAttribute(context: Context, constant: Int) = TypedValue().apply {
    context.theme.resolveAttribute(constant, this, true)
}.data

fun getColorPaint(context: Context, constant: Int) = createPaint().apply {
    isAntiAlias = true
    style = Paint.Style.FILL
    color = getThemeAttribute(context, constant)
}
