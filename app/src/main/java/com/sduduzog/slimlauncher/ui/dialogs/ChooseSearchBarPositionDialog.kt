package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.jkuester.unlauncher.datastore.SearchBarPosition
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseSearchBarPositionDialog : DialogFragment() {

    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val repo = unlauncherDataSource.corePreferencesRepo
        val active = repo.get().searchBarPosition.number
        builder.setTitle(R.string.choose_search_bar_position_dialog_title)
        builder.setSingleChoiceItems(
            R.array.search_bar_position_array,
            active
        ) { dialogInterface, i ->
            dialogInterface.dismiss()
            repo.updateSearchBarPosition(SearchBarPosition.forNumber(i))
        }
        return builder.create()
    }

    companion object {
        fun getSearchBarPositionChooser(): ChooseSearchBarPositionDialog =
            ChooseSearchBarPositionDialog()
    }
}
