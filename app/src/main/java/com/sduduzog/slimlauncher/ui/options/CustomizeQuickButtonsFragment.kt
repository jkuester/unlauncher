package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jkuester.unlauncher.datasource.DataRepository
import com.jkuester.unlauncher.datasource.QuickButtonIcon
import com.jkuester.unlauncher.datasource.getIconResourceId
import com.jkuester.unlauncher.datastore.proto.QuickButtonPreferences
import com.jkuester.unlauncher.dialog.QuickButtonIconDialog
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsFragmentBinding
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeQuickButtonsFragment : BaseFragment() {
    @Inject
    lateinit var quickButtonPreferencesRepo: DataRepository<QuickButtonPreferences>

    override fun getFragmentView(): ViewGroup = CustomizeQuickButtonsFragmentBinding.bind(
        requireView()
    ).customizeQuickButtonsFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.customize_quick_buttons_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customizeQuickButtonsFragment = CustomizeQuickButtonsFragmentBinding.bind(view)
        quickButtonPreferencesRepo.observe { prefs ->
            customizeQuickButtonsFragment.customizeQuickButtonsFragmentLeft
                .setImageResource(getIconResourceId(prefs.leftButton.iconId)!!)
            customizeQuickButtonsFragment.customizeQuickButtonsFragmentCenter
                .setImageResource(getIconResourceId(prefs.centerButton.iconId)!!)
            customizeQuickButtonsFragment.customizeQuickButtonsFragmentRight
                .setImageResource(getIconResourceId(prefs.rightButton.iconId)!!)
        }

        customizeQuickButtonsFragment.customizeQuickButtonsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        customizeQuickButtonsFragment.customizeQuickButtonsFragmentLeft.setOnClickListener {
            QuickButtonIconDialog(
                QuickButtonIcon.IC_CALL.prefId
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customizeQuickButtonsFragment.customizeQuickButtonsFragmentCenter.setOnClickListener {
            QuickButtonIconDialog(
                QuickButtonIcon.IC_COG.prefId
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customizeQuickButtonsFragment.customizeQuickButtonsFragmentRight.setOnClickListener {
            QuickButtonIconDialog(
                QuickButtonIcon.IC_PHOTO_CAMERA.prefId
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
    }
}
