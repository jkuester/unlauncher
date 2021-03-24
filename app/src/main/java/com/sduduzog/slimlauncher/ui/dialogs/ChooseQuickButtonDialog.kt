package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class ChooseQuickButtonDialog(private var settingsKey: Int, private var defaultIconId: Int) : DialogFragment() {
    private lateinit var settings: SharedPreferences
    private var onDismissListener: DialogInterface.OnDismissListener? = null
    private val iconIdsByIndex = mapOf(0 to defaultIconId, 1 to R.drawable.ic_empty)
    private val indexesByIconId = iconIdsByIndex.entries.associate { it.value to it.key }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)

        val currentIconId = settings.getInt(getString(settingsKey), defaultIconId)

        builder.setTitle(R.string.options_fragment_customize_quick_buttons)

        builder.setSingleChoiceItems(R.array.quick_button_array, indexesByIconId[currentIconId]!!) { dialogInterface, i ->
            dialogInterface.dismiss()
            settings.edit {
                putInt(getString(settingsKey), iconIdsByIndex[i]!!)
            }

        }
        return builder.create()
    }

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?) {
        this.onDismissListener = onDismissListener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }
}