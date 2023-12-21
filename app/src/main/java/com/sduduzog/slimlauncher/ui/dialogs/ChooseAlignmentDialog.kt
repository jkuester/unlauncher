package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class ChooseAlignmentDialog : DialogFragment() {

    private lateinit var settings: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(getString(R.string.prefs_settings), MODE_PRIVATE)

        val active = settings.getInt(getString(R.string.prefs_settings_alignment), 3)
        builder.setTitle(R.string.choose_alignment_dialog_title)
        builder.setSingleChoiceItems(R.array.alignment_format_array, active) { dialogInterface, i ->
            dialogInterface.dismiss()
            settings.edit {
                putInt(getString(R.string.prefs_settings_alignment), i)
            }

        }
        return builder.create()
    }


    companion object {
        fun getInstance(): ChooseAlignmentDialog{
            return ChooseAlignmentDialog()
        }
    }
}

