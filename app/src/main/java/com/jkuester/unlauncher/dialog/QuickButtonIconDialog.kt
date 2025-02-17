package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.setCenterIconId
import com.jkuester.unlauncher.datasource.setLeftIconId
import com.jkuester.unlauncher.datasource.setRightIconId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.sduduzog.slimlauncher.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private fun getCurrentIconIdByDefault(defaultIconId: Int) = { quickButtonPrefs: QuickButtonPreferences ->
    when (defaultIconId) {
        QuickButtonIcon.IC_CALL.prefId -> quickButtonPrefs.leftButton.iconId
        QuickButtonIcon.IC_COG.prefId -> quickButtonPrefs.centerButton.iconId
        else -> quickButtonPrefs.rightButton.iconId
    }
}

private fun getIndexByIconId(iconId: Int) = when (iconId) {
    QuickButtonIcon.IC_EMPTY.prefId -> 1
    else -> 0
}

private fun getIconIdByIndex(defaultIconId: Int, index: Int) = when (index) {
    1 -> QuickButtonIcon.IC_EMPTY.prefId
    else -> defaultIconId
}

private fun getUpdateFunctionByDefault(defaultIconId: Int) = when (defaultIconId) {
    QuickButtonIcon.IC_CALL.prefId -> ::setLeftIconId
    QuickButtonIcon.IC_COG.prefId -> ::setCenterIconId
    else -> ::setRightIconId
}

@AndroidEntryPoint
class QuickButtonIconDialog(private val defaultIconId: Int) : DialogFragment() {
    @Inject
    lateinit var repo: DataRepository<QuickButtonPreferences>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog
        .Builder(context)
        .setTitle(R.string.options_fragment_customize_quick_buttons)
        .setSingleChoiceItems(
            R.array.quick_button_array,
            getCurrentIndex(),
            this::onSelection
        )
        .create()

    private fun getCurrentIndex() = repo
        .get()
        .let(getCurrentIconIdByDefault(defaultIconId))
        .let(::getIndexByIconId)

    private fun onSelection(dialogInterface: DialogInterface, i: Int) = dialogInterface
        .dismiss()
        .also {
            getIconIdByIndex(defaultIconId, i)
                .let(getUpdateFunctionByDefault(defaultIconId))
                .let(repo::updateAsync)
        }
}
