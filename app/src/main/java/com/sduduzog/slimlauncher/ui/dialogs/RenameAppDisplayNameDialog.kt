package com.sduduzog.slimlauncher.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datastore.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.apps.UnlauncherAppsRepository
import kotlinx.android.synthetic.main.customise_apps_fragment.*

class RenameAppDisplayNameDialog : DialogFragment() {
    private lateinit var app: UnlauncherApp
    private lateinit var unlauncherAppsRepo: UnlauncherAppsRepository

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.rename_dialog_edit_text, customise_apps_fragment, false)
        val editText: EditText = view.findViewById(R.id.rename_editText)
        val appName: String = app.displayName
        editText.text.append(appName)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rename $appName")
        builder.setView(view)
        builder.setPositiveButton("DONE") { _, _ ->
            val name = editText.text.toString()
            updateApp(name)
        }
        editText.setOnEditorActionListener { v, _, _ ->
            val name = v.text.toString()
            updateApp(name)
            this@RenameAppDisplayNameDialog.dismiss()
            true
        }

        return builder.create()
    }

    private fun updateApp(newName: String) {
        if (newName.isNotEmpty()) {
            unlauncherAppsRepo.updateDisplayName(app, newName)
        } else {
            Toast.makeText(context, "Couldn't save, App name shouldn't be empty", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun getInstance(app: UnlauncherApp, unlauncherAppsRepo : UnlauncherAppsRepository) : RenameAppDisplayNameDialog {
            return RenameAppDisplayNameDialog().apply {
                this.app = app
                this.unlauncherAppsRepo = unlauncherAppsRepo
            }
        }
    }
}