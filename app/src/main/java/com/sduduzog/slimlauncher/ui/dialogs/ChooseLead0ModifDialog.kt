package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class ChooseLead0ModifDialog : DialogFragment() {

    private lateinit var settings: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(
            getString(R.string.prefs_settings),
            MODE_PRIVATE
        )

        val active = settings.getInt(getString(R.string.prefs_settings_key_lead0_modif), 0)
        builder.setTitle(R.string.choose_lead0_modif_dialog_title)
        builder.setSingleChoiceItems(R.array.lead0_modif_array, active) { dialogInterface, i ->
            dialogInterface.dismiss()
            settings.edit {
                putInt(getString(R.string.prefs_settings_key_lead0_modif), i)
            }
        }
        return builder.create()
    }

    companion object {
        fun getInstance(): ChooseLead0ModifDialog {
            return ChooseLead0ModifDialog()
        }
    }
}
