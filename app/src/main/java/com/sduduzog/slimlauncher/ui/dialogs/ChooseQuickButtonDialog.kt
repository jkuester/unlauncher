package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.QuickButtonPreferencesRepository

class ChooseQuickButtonDialog(
    private val repo: QuickButtonPreferencesRepository,
    private val defaultIconId: Int
) : DialogFragment() {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private val iconIdsByIndex = mapOf(0 to defaultIconId, 1 to R.drawable.ic_empty)
    private val indexesByIconId = iconIdsByIndex.entries.associate { it.value to it.key }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val quickButtonPrefs = repo.get()
        var currentIconId = 0
        when (defaultIconId) {
            QuickButtonPreferencesRepository.DEFAULT_ICON_LEFT -> currentIconId =
                quickButtonPrefs.leftButton.iconId
            QuickButtonPreferencesRepository.DEFAULT_ICON_CENTER -> currentIconId =
                quickButtonPrefs.centerButton.iconId
            QuickButtonPreferencesRepository.DEFAULT_ICON_RIGHT -> currentIconId =
                quickButtonPrefs.rightButton.iconId
        }

        builder.setTitle(R.string.options_fragment_customize_quick_buttons)

        builder.setSingleChoiceItems(
            R.array.quick_button_array,
            indexesByIconId[currentIconId]!!
        ) { dialogInterface, i ->
            dialogInterface.dismiss()
            when (defaultIconId) {
                QuickButtonPreferencesRepository.DEFAULT_ICON_LEFT -> repo.updateLeftIconId(
                    iconIdsByIndex[i]!!
                )
                QuickButtonPreferencesRepository.DEFAULT_ICON_CENTER -> repo.updateCenterIconId(
                    iconIdsByIndex[i]!!
                )
                QuickButtonPreferencesRepository.DEFAULT_ICON_RIGHT -> repo.updateRightIconId(
                    iconIdsByIndex[i]!!
                )
            }
        }
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }
}