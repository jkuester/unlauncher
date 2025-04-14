package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.annotation.ColorRes
import androidx.core.text.set
import androidx.core.text.toSpannable
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.setTheme
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.Theme
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.utils.getColorCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThemeDialog : DialogFragment() {
    @Inject @WithFragmentLifecycle
    lateinit var corePreferencesRepo: DataRepository<CorePreferences>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adapter = object : ArrayAdapter<CharSequence>(
            requireContext(),
            R.layout.adapter_item_theme,
            resources.getTextArray(R.array.themes_array)
        ) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val radioButton = super.getView(position, convertView, parent) as RadioButton
                // The 'system default' should not have any color previews
                val themePreviewColors = getThemePreviewColors(position) ?: return radioButton
                populateRadioButtonWithColorPreviews(radioButton, getItem(position), themePreviewColors)
                return radioButton
            }

            private fun populateRadioButtonWithColorPreviews(
                radioButton: RadioButton,
                text: CharSequence?,
                colors: Array<Int>
            ) {
                val textSize = radioButton.textSize.toInt()
                val previewDrawable = ThemeColorPreviewDrawable(
                    sizePx = textSize,
                    strokeWidthPx = radioButton.context.resources.getDimension(R.dimen._1sdp),
                    bgColor = context.getColorCompat(colors[0]),
                    headerColor = context.getColorCompat(colors[1]),
                    fontColor = context.getColorCompat(colors[2]),
                    strokeColor = context.getColorCompat(R.color.colorGray)
                )
                previewDrawable.setBounds(
                    0,
                    0,
                    previewDrawable.intrinsicWidth,
                    previewDrawable.intrinsicHeight
                )

                val sSB = SpannableStringBuilder()
                sSB.append(text).append(" ")
                val start = sSB.length
                sSB.append(" ")
                sSB[start, start + 1] = ImageSpan(previewDrawable, ImageSpan.ALIGN_BOTTOM)

                radioButton.text = sSB.toSpannable()
            }
        }

        val al = AlertDialog
            .Builder(context)
            .setTitle(R.string.choose_theme_dialog_title)
            .setSingleChoiceItems(
                adapter,
                corePreferencesRepo.get().theme.number,
                this::onSelection
            )
            .create()

        return al
    }

    private fun onSelection(dialogInterface: DialogInterface, i: Int) = dialogInterface
        .dismiss()
        .also { corePreferencesRepo.updateAsync(setTheme(Theme.forNumber(i))) }

    private fun getThemePreviewColors(listPosition: Int): Array<Int>? {
        val colors = Array(3) { _ -> 0 }
        when (listPosition) {
            // System default
            0 -> return null
            // Midnight
            1 -> {
                fillArray(
                    android.R.color.black,
                    R.color.colorChineseWhite,
                    R.color.colorChineseWhite,
                    colors
                )
            }
            // Jupiter
            2 -> {
                fillArray(
                    R.color.colorBlueGrey,
                    R.color.colorChineseWhite,
                    R.color.colorGray,
                    colors
                )
            }
            // teal
            3 -> {
                fillArray(
                    R.color.colorTeal,
                    R.color.colorVampireBlack,
                    R.color.colorGray,
                    colors
                )
            }
            // Candy
            4 -> {
                fillArray(
                    R.color.colorCandy,
                    R.color.colorChineseWhite,
                    R.color.colorGray,
                    colors
                )
            }
            // Pastel
            5 -> {
                fillArray(
                    R.color.colorPink,
                    R.color.colorVampireBlack,
                    R.color.colorGray,
                    colors
                )
            }
            // Noon
            6 -> {
                fillArray(
                    android.R.color.white,
                    R.color.colorVampireBlack,
                    R.color.colorGray,
                    colors
                )
            }
            // Vlad
            7 -> {
                fillArray(
                    R.color.colorGunmetal,
                    R.color.colorDarkBlueGray,
                    R.color.colorCultured,
                    colors
                )
            }
            // Groovy
            8 -> {
                fillArray(
                    R.color.colorCharlestonGreen,
                    R.color.colorAcidGreen,
                    R.color.colorCookiesAndCream,
                    colors
                )
            }
        }
        return colors
    }

    private fun fillArray(
        @ColorRes bgColor: Int,
        @ColorRes hdColor: Int,
        @ColorRes fontColor: Int,
        colors: Array<Int>
    ) {
        colors[0] = bgColor
        colors[1] = hdColor
        colors[2] = fontColor
    }
}
