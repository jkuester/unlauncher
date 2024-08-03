package com.sduduzog.slimlauncher.ui.options

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.databinding.OptionsFragmentBinding
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.ui.dialogs.ChangeThemeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseAlignmentDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseClockTypeDialog
import com.sduduzog.slimlauncher.ui.dialogs.ChooseTimeFormatDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OptionsFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = OptionsFragmentBinding.bind(
        requireView()
    ).optionsFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.options_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val optionsFragment = OptionsFragmentBinding.bind(requireView())
        optionsFragment.optionsFragmentBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        optionsFragment.optionsFragmentChangeTheme.setOnClickListener {
            val changeThemeDialog = ChangeThemeDialog.getThemeChooser()
            changeThemeDialog.showNow(childFragmentManager, "THEME_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseTimeFormat.setOnClickListener {
            val chooseTimeFormatDialog = ChooseTimeFormatDialog.getInstance()
            chooseTimeFormatDialog.showNow(childFragmentManager, "TIME_FORMAT_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseClockType.setOnClickListener {
            val chooseClockTypeDialog = ChooseClockTypeDialog.getInstance()
            chooseClockTypeDialog.showNow(childFragmentManager, "CLOCK_TYPE_CHOOSER")
        }
        optionsFragment.optionsFragmentChooseAlignment.setOnClickListener {
            val chooseAlignmentDialog = ChooseAlignmentDialog.getInstance()
            chooseAlignmentDialog.showNow(childFragmentManager, "ALIGNMENT_CHOOSER")
        }
        optionsFragment.optionsFragmentToggleStatusBar.setOnClickListener {
            val settings = requireContext().getSharedPreferences(
                getString(R.string.prefs_settings),
                MODE_PRIVATE
            )
            val isHidden = settings.getBoolean(
                getString(R.string.prefs_settings_key_toggle_status_bar),
                false
            )
            settings.edit {
                putBoolean(getString(R.string.prefs_settings_key_toggle_status_bar), !isHidden)
            }
        }
        optionsFragment.optionsFragmentCustomizeApps.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeAppsFragment
            )
        )
        optionsFragment.optionsFragmentCustomizeQuickButtons.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeQuickButtonsFragment
            )
        )
        optionsFragment.optionsFragmentCustomizeAppDrawer.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_optionsFragment_to_customizeAppDrawerFragment
            )
        )
    }
}
