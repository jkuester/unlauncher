package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.CustomizeQuickButtonsFragmentBinding
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.datasource.quickbuttonprefs.QuickButtonPreferencesRepository
import com.sduduzog.slimlauncher.ui.dialogs.ChooseQuickButtonDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeQuickButtonsFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = CustomizeQuickButtonsFragmentBinding.bind(
        requireView()
    ).customizeQuickButtonsFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_quick_buttons_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefsRepo = unlauncherDataSource.quickButtonPreferencesRepo

        val customizeQuickButtonsFragment = CustomizeQuickButtonsFragmentBinding.bind(view)
        prefsRepo.liveData().observe(viewLifecycleOwner) { prefs ->
            customizeQuickButtonsFragment.customizeQuickButtonsFragmentLeft
                .setImageResource(
                    QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.leftButton.iconId)
                )
            customizeQuickButtonsFragment.customizeQuickButtonsFragmentCenter
                .setImageResource(
                    QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.centerButton.iconId)
                )
            customizeQuickButtonsFragment.customizeQuickButtonsFragmentRight
                .setImageResource(
                    QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.rightButton.iconId)
                )
        }

        customizeQuickButtonsFragment.customizeQuickButtonsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        customizeQuickButtonsFragment.customizeQuickButtonsFragmentLeft.setOnClickListener {
            ChooseQuickButtonDialog(
                prefsRepo,
                QuickButtonPreferencesRepository.IC_CALL
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customizeQuickButtonsFragment.customizeQuickButtonsFragmentCenter.setOnClickListener {
            ChooseQuickButtonDialog(
                prefsRepo,
                QuickButtonPreferencesRepository.IC_COG
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customizeQuickButtonsFragment.customizeQuickButtonsFragmentRight.setOnClickListener {
            ChooseQuickButtonDialog(
                prefsRepo,
                QuickButtonPreferencesRepository.IC_PHOTO_CAMERA
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
    }
}
