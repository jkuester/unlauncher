package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.setDisplayName
import com.jkuester.unlauncher.datastore.proto.UnlauncherApp
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.fragment.Supplier
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.RenameDialogEditTextBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RenameAppDisplayNameDialog(private val app: UnlauncherApp) : DialogFragment() {
    @Inject
    lateinit var layoutInflaterSupplier: Supplier<LayoutInflater>
    @Inject
    lateinit var unlauncherAppsRepo: DataRepository<UnlauncherApps>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = RenameDialogEditTextBinding.inflate(layoutInflaterSupplier.get())
        val editText: EditText = binding.renameEditText
        editText.setText(app.displayName)
        return AlertDialog
            .Builder(context)
            .setTitle(R.string.rename_app)
            .setView(binding.root)
            .setPositiveButton(R.string.menu_rename) { dialog, _ ->
                editText.text
                    .toString()
                    .let(this::updateApp)
                dialog.dismiss()
            }
            .create()
    }

    private fun updateApp(newName: String) {
        if (newName.isEmpty()) {
            return
        }
        unlauncherAppsRepo.updateAsync(setDisplayName(app, newName))
    }
}
