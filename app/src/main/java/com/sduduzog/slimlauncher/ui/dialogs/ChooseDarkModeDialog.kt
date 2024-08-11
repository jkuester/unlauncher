package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class ChooseDarkModeDialog : DialogFragment() {

    private lateinit var settings: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(
            getString(R.string.prefs_settings),
            MODE_PRIVATE
        )

        val active = settings.getInt(getString(R.string.prefs_settings_key_dark_mode), 0)
        builder.setTitle(R.string.choose_dark_mode_dialog_title)
        builder.setSingleChoiceItems(R.array.dark_mode_array, active) { dialogInterface, i ->
            dialogInterface.dismiss()
            settings.edit {
                putInt(getString(R.string.prefs_settings_key_dark_mode), i)
            }
        }
        return builder.create()
    }

    companion object {
        fun getInstance(): ChooseDarkModeDialog {
            return ChooseDarkModeDialog()
        }
    }
}
