package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.setTheme
import com.jkuester.unlauncher.datastore.proto.CorePreferences
import com.jkuester.unlauncher.datastore.proto.Theme
import com.jkuester.unlauncher.fragment.WithFragmentLifecycle
import com.sduduzog.slimlauncher.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThemeDialog : DialogFragment() {
    @Inject @WithFragmentLifecycle
    lateinit var corePreferencesRepo: DataRepository<CorePreferences>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog
        .Builder(context)
        .setTitle(R.string.choose_theme_dialog_title)
        .setSingleChoiceItems(
            R.array.themes_array,
            corePreferencesRepo.get().theme.number,
            this::onSelection
        )
        .create()

    private fun onSelection(dialogInterface: DialogInterface, i: Int) = dialogInterface
        .dismiss()
        .also { corePreferencesRepo.updateAsync(setTheme(Theme.forNumber(i))) }
}
