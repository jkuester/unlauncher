package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.datasource.quickbuttonprefs.QuickButtonPreferencesRepository
import com.sduduzog.slimlauncher.ui.dialogs.ChooseQuickButtonDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_quick_buttons_fragment.customize_quick_buttons_fragment
import kotlinx.android.synthetic.main.customize_quick_buttons_fragment.customize_quick_buttons_fragment_back
import kotlinx.android.synthetic.main.customize_quick_buttons_fragment.customize_quick_buttons_fragment_center
import kotlinx.android.synthetic.main.customize_quick_buttons_fragment.customize_quick_buttons_fragment_left
import kotlinx.android.synthetic.main.customize_quick_buttons_fragment.customize_quick_buttons_fragment_right
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeQuickButtonsFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = customize_quick_buttons_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_quick_buttons_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefsRepo = unlauncherDataSource.quickButtonPreferencesRepo

        prefsRepo.liveData().observe(viewLifecycleOwner) { prefs ->
            customize_quick_buttons_fragment_left
                .setImageResource(QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.leftButton.iconId))
            customize_quick_buttons_fragment_center
                .setImageResource(QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.centerButton.iconId))
            customize_quick_buttons_fragment_right
                .setImageResource(QuickButtonPreferencesRepository.RES_BY_ICON.getValue(prefs.rightButton.iconId))
        }

        customize_quick_buttons_fragment_back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        customize_quick_buttons_fragment_left.setOnClickListener {
            ChooseQuickButtonDialog(
                prefsRepo,
                QuickButtonPreferencesRepository.IC_CALL
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customize_quick_buttons_fragment_center.setOnClickListener {
            ChooseQuickButtonDialog(
                prefsRepo,
                QuickButtonPreferencesRepository.IC_COG
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customize_quick_buttons_fragment_right.setOnClickListener {
            ChooseQuickButtonDialog(
                prefsRepo,
                QuickButtonPreferencesRepository.IC_PHOTO_CAMERA
            ).showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
    }
}
