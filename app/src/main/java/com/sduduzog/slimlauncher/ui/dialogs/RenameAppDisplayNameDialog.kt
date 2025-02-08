package com.sduduzog.slimlauncher.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.UnlauncherAppsRepository
import com.jkuester.unlauncher.datasource.setDisplayName
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.RenameDialogEditTextBinding

class RenameAppDisplayNameDialog : DialogFragment() {
    private lateinit var app: UnlauncherApp
    private lateinit var unlauncherAppsRepo: UnlauncherAppsRepository

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = RenameDialogEditTextBinding.inflate(layoutInflater).root
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
            unlauncherAppsRepo.updateAsync(setDisplayName(app, newName))
        } else {
            Toast.makeText(
                context,
                "Couldn't save, App name shouldn't be empty",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        fun getInstance(app: UnlauncherApp, unlauncherAppsRepo: UnlauncherAppsRepository): RenameAppDisplayNameDialog =
            RenameAppDisplayNameDialog().apply {
                this.app = app
                this.unlauncherAppsRepo = unlauncherAppsRepo
            }
    }
}
