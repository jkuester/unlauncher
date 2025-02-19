package com.jkuester.unlauncher.bindings

import android.view.View.OnClickListener
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentManager
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.getIconResourceId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.dialog.QuickButtonIconDialog
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsBinding

fun setupCustomizeQuickButtonsBackButton(activity: ComponentActivity) = { options: CustomizeQuickButtonsBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

private fun updateQuickButtonIcons(binding: CustomizeQuickButtonsBinding): (QuickButtonPreferences) -> Unit = { prefs ->
    prefs.leftButton.iconId
        .let(::getIconResourceId)
        ?.let(binding.quickButtonLeft::setImageResource)
    prefs.centerButton.iconId
        .let(::getIconResourceId)
        ?.let(binding.quickButtonCenter::setImageResource)
    prefs.rightButton.iconId
        .let(::getIconResourceId)
        ?.let(binding.quickButtonRight::setImageResource)
}

private fun showQuickButtonIconDialog(icon: QuickButtonIcon, fragmentManager: FragmentManager) = OnClickListener {
    QuickButtonIconDialog(icon.prefId).showNow(fragmentManager, null)
}

fun setupQuickButtonIcons(prefsRepo: DataRepository<QuickButtonPreferences>, fragmentManager: FragmentManager) =
    { binding: CustomizeQuickButtonsBinding ->
        prefsRepo.observe(updateQuickButtonIcons(binding))
        binding.quickButtonLeft.setOnClickListener(showQuickButtonIconDialog(QuickButtonIcon.IC_CALL, fragmentManager))
        binding.quickButtonCenter.setOnClickListener(showQuickButtonIconDialog(QuickButtonIcon.IC_COG, fragmentManager))
        binding.quickButtonRight.setOnClickListener(
            showQuickButtonIconDialog(QuickButtonIcon.IC_PHOTO_CAMERA, fragmentManager)
        )
    }
