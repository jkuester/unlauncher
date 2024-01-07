package com.sduduzog.slimlauncher.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.RenameDialogEditTextBinding
import com.sduduzog.slimlauncher.models.CustomiseAppsViewModel
import com.sduduzog.slimlauncher.models.HomeApp

class RenameAppDialog : DialogFragment() {

    private lateinit var app: HomeApp
    private lateinit var model: CustomiseAppsViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = RenameDialogEditTextBinding.inflate(layoutInflater).root
        val editText: EditText = view.findViewById(R.id.rename_editText)
        val appName: String = app.appNickname ?: app.appName
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
            this@RenameAppDialog.dismiss()
            true
        }
        return builder.create()
    }

    private fun updateApp(newName: String) {
        if (newName.isNotEmpty()) {
            app.appNickname = newName
            model.update(app)
        } else {
            Toast.makeText(context, "Couldn't save, App name shouldn't be empty", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun getInstance(app: HomeApp, model: CustomiseAppsViewModel): RenameAppDialog {
            return RenameAppDialog().apply {
                this.model = model
                this.app = app
            }
        }
    }
}