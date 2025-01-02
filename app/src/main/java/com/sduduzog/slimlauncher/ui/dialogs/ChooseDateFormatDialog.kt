package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class ChooseDateFormatDialog : DialogFragment() {

    private lateinit var settings: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(
            getString(R.string.prefs_settings),
            MODE_PRIVATE
        )

        val active = settings.getInt(getString(R.string.prefs_settings_key_date_format), 0)
        builder.setTitle(R.string.choose_date_format_dialog_title)
        builder.setSingleChoiceItems(R.array.date_format_array, active) { dialogInterface, i ->
            dialogInterface.dismiss()
            settings.edit {
                putInt(getString(R.string.prefs_settings_key_date_format), i)
            }
        }
        return builder.create()
    }

    companion object {
        fun getInstance(): ChooseDateFormatDialog {
            return ChooseDateFormatDialog()
        }
    }
}
