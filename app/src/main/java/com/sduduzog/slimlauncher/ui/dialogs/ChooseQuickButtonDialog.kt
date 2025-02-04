package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.QuickButtonPreferencesRepository
import com.jkuester.unlauncher.datasource.setCenterIconId
import com.jkuester.unlauncher.datasource.setLeftIconId
import com.jkuester.unlauncher.datasource.setRightIconId
import com.sduduzog.slimlauncher.R

class ChooseQuickButtonDialog(
    private val repo: QuickButtonPreferencesRepository,
    private val defaultIconId: Int,
) : DialogFragment() {
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private val iconIdsByIndex =
        mapOf(0 to defaultIconId, 1 to QuickButtonIcon.IC_EMPTY.prefId)
    private val indexesByIconId = iconIdsByIndex.entries.associate { it.value to it.key }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val quickButtonPrefs = repo.get()
        var currentIconId = 0
        when (defaultIconId) {
            QuickButtonIcon.IC_CALL.prefId ->
                currentIconId =
                    quickButtonPrefs.leftButton.iconId

            QuickButtonIcon.IC_COG.prefId ->
                currentIconId =
                    quickButtonPrefs.centerButton.iconId

            QuickButtonIcon.IC_PHOTO_CAMERA.prefId ->
                currentIconId =
                    quickButtonPrefs.rightButton.iconId
        }

        builder.setTitle(R.string.options_fragment_customize_quick_buttons)

        builder.setSingleChoiceItems(
            R.array.quick_button_array,
            indexesByIconId[currentIconId]!!,
        ) { dialogInterface, i ->
            dialogInterface.dismiss()
            when (defaultIconId) {
                QuickButtonIcon.IC_CALL.prefId ->
                    repo.updateAsync(
                        setLeftIconId(iconIdsByIndex[i]!!),
                    )

                QuickButtonIcon.IC_COG.prefId ->
                    repo.updateAsync(
                        setCenterIconId(iconIdsByIndex[i]!!),
                    )

                QuickButtonIcon.IC_PHOTO_CAMERA.prefId ->
                    repo.updateAsync(
                        setRightIconId(iconIdsByIndex[i]!!),
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
