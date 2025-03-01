package com.jkuester.unlauncher.bindings

import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import com.jkuester.unlauncher.adapter.CustomizeHomeAppsListAdapter
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.getHomeApps
import com.jkuester.unlauncher.datasource.getIconResourceId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.datastore.proto.UnlauncherApps
import com.jkuester.unlauncher.dialog.QuickButtonIconDialog
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsBinding

fun setupCustomizeQuickButtonsBackButton(activity: ComponentActivity) = { options: CustomizeQuickButtonsBinding ->
    options.headerBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
}

private fun setIconResource(iconView: ImageView) = { resourceId: Int ->
    iconView.setImageResource(resourceId)
    when (resourceId) {
        R.drawable.ic_empty -> iconView.setBackgroundResource(R.drawable.imageview_border)
        else -> iconView.setBackgroundResource(0)
    }
}

private fun updateQuickButtonIcons(binding: CustomizeQuickButtonsBinding): (QuickButtonPreferences) -> Unit = { prefs ->
    prefs.leftButton.iconId
        .let(::getIconResourceId)
        ?.let(setIconResource(binding.quickButtonLeft))
    prefs.centerButton.iconId
        .let(::getIconResourceId)
        ?.let(setIconResource(binding.quickButtonCenter))
    prefs.rightButton.iconId
        .let(::getIconResourceId)
        ?.let(setIconResource(binding.quickButtonRight))
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

fun setupAddHomeAppButton(appsRepo: DataRepository<UnlauncherApps>): (CustomizeQuickButtonsBinding) -> Unit =
    { binding ->
        appsRepo.observe {
            if (getHomeApps(it).size > 5) {
                binding.addHomeApp.visibility = View.GONE
            } else {
                binding.addHomeApp.visibility = View.VISIBLE
            }
        }

        Navigation
            .createNavigateOnClickListener(R.id.customiseQuickButtonsFragment_to_customizeHomeAppsAddAppFragment)
            .also(binding.addHomeApp::setOnClickListener)
    }

fun setupHomeAppsList(appsRepo: DataRepository<UnlauncherApps>) = { binding: CustomizeQuickButtonsBinding ->
    val adapter = CustomizeHomeAppsListAdapter(appsRepo)
    binding.customiseHomeAppsList.adapter = adapter
}
