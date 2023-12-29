package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datastore.ClockType
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseClockTypeDialog : DialogFragment(){

    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val repo = unlauncherDataSource.corePreferencesRepo
        val active = repo.get().clockType.number
        builder.setTitle(R.string.choose_clock_type_dialog_title)
        builder.setSingleChoiceItems(R.array.clock_type_array, active) {dialogInterface, i ->
            dialogInterface.dismiss()
            repo.updateClockType(ClockType.forNumber(i))
        }
        return builder.create()
    }

    companion object {
        fun getInstance(): ChooseClockTypeDialog = ChooseClockTypeDialog()
    }
}