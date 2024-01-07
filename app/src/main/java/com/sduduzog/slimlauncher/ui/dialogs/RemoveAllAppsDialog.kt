package com.sduduzog.slimlauncher.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.models.CustomiseAppsViewModel
import com.sduduzog.slimlauncher.models.HomeApp

class RemoveAllAppsDialog : DialogFragment() {

    private lateinit var apps: List<HomeApp>
    private lateinit var model: CustomiseAppsViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.remove_all_apps_dialog_title)
        builder.setMessage(R.string.remove_all_apps_dialog_message)
        builder.setPositiveButton("OK") { _, _ ->
            model.clearTable()
        }
        return builder.create()
    }

    companion object {
        fun getInstance(apps: List<HomeApp>, model: CustomiseAppsViewModel): RemoveAllAppsDialog {
            return RemoveAllAppsDialog().apply {
                this.apps = apps
                this.model = model
            }
        }
    }
}
