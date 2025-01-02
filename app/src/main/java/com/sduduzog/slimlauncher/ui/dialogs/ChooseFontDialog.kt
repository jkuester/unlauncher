package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class ChooseFontDialog : DialogFragment() {

    private lateinit var settings: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(
            getString(R.string.prefs_settings),
            MODE_PRIVATE
        )

        // if not set, use setting 1 (Ubuntu) as default, because that was the
        // previous app version's default
        val active = settings.getInt(getString(R.string.prefs_settings_key_font), 0)
        builder.setTitle(R.string.choose_font_dialog_title)
        builder.setSingleChoiceItems(R.array.font_array, active) { dialogInterface, i ->
            dialogInterface.dismiss()
            settings.edit {
                putInt(getString(R.string.prefs_settings_key_font), i)
            }
        }
        return builder.create()
    }

    companion object {
        fun getInstance(): ChooseFontDialog {
            return ChooseFontDialog()
        }
    }
}
