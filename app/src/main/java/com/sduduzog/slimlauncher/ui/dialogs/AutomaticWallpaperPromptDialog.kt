package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.DialogInterface.*
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R

class AutomaticWallpaperPromptDialog : DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var settings: SharedPreferences
    private var listener: OnPromptResultListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            listener = activity as OnPromptResultListener
        } finally {
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        settings = requireContext().getSharedPreferences(getString(R.string.prefs_key_automatic_wallpaper_shown), MODE_PRIVATE)

        builder.setTitle(R.string.ask_automatic_wallpaper_dialog_title)
        builder.setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, this)
            .setNeutralButton(R.string.ask_automatic_wallpaper_never, this)

        return builder.create()
    }

    private fun setFlag(flag: Boolean) {
        settings.edit {
            putBoolean(getString(R.string.prefs_key_automatic_wallpaper_shown), flag)
        }
    }

    companion object {
        fun getAutomaticWallpaperPrompt(): AutomaticWallpaperPromptDialog {
            return AutomaticWallpaperPromptDialog()
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> {
                listener?.yes()
                setFlag(true)
            }
            BUTTON_NEUTRAL -> {
                setFlag(true)
            }
        }
        dialog?.dismiss()
    }

    interface OnPromptResultListener {
        fun yes()
    }
}