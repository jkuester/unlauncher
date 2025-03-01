package com.jkuester.unlauncher.bindings

import android.view.View.OnClickListener
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import com.jkuester.unlauncher.adapter.CustomizeHomeAppsListAdapter
import com.jkuester.unlauncher.adapter.CustomizeHomeAppsListAdapter.ViewHolder
import com.jkuester.unlauncher.adapter.DragSortableCallback
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.getIconResourceId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.dialog.QuickButtonIconDialog
import com.sduduzog.slimlauncher.R
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

fun setupAddHomeAppButton(binding: CustomizeQuickButtonsBinding) = Navigation
    .createNavigateOnClickListener(R.id.customiseQuickButtonsFragment_to_customizeHomeAppsAddAppFragment)
    .also(binding.addHomeApp::setOnClickListener)
    .also(binding.addHomeAppPlus::setOnClickListener)

fun setupHomeAppsList(appsRepo: DataRepository<UnlauncherApps>) = { binding: CustomizeQuickButtonsBinding ->
    var touchHelper: ItemTouchHelper? = null
    val startDragListener: (ViewHolder) -> Boolean = { holder ->
        touchHelper?.startDrag(holder)
        false
    }

    val adapter = CustomizeHomeAppsListAdapter(appsRepo, startDragListener)
    val homeAppsMoveCallback = DragSortableCallback(adapter)

    touchHelper = ItemTouchHelper(homeAppsMoveCallback)
    touchHelper.attachToRecyclerView(binding.customiseHomeAppsList)
    binding.customiseHomeAppsList.adapter = adapter
}
