package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datasource.CorePreferencesRepository
import com.jkuester.unlauncher.datasource.setAlignmentFormat
import com.jkuester.unlauncher.datastore.proto.AlignmentFormat
import com.sduduzog.slimlauncher.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseAlignmentDialog : DialogFragment() {

    @Inject
    lateinit var corePreferencesRepo: CorePreferencesRepository

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val active = corePreferencesRepo.get().alignmentFormat.number
        builder.setTitle(R.string.choose_alignment_dialog_title)
        builder.setSingleChoiceItems(R.array.alignment_format_array, active) { dialogInterface, i ->
            dialogInterface.dismiss()
            corePreferencesRepo.updateAsync(setAlignmentFormat(AlignmentFormat.forNumber(i)))
        }
        return builder.create()
    }

    companion object {
        fun getInstance(): ChooseAlignmentDialog = ChooseAlignmentDialog()
    }
}
