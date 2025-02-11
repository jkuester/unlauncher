package com.jkuester.unlauncher.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setAlignmentFormat
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.sduduzog.slimlauncher.R

class AlignmentFormatDialog(private val corePreferencesRepo: CorePreferencesRepository) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog
        .Builder(context)
        .setTitle(R.string.choose_alignment_dialog_title)
        .setSingleChoiceItems(
            R.array.alignment_format_array,
            corePreferencesRepo.get().alignmentFormat.number,
            this::selectAlignment
        )
        .create()

    private fun selectAlignment(dialogInterface: DialogInterface, i: Int) = dialogInterface
        .dismiss()
        .also { corePreferencesRepo.updateAsync(setAlignmentFormat(AlignmentFormat.forNumber(i))) }
}
