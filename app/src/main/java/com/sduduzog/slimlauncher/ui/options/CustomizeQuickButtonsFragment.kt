package com.sduduzog.slimlauncher.ui.options

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.ui.dialogs.ChooseQuickButtonDialog
import com.sduduzog.slimlauncher.utils.BaseFragment
import kotlinx.android.synthetic.main.customize_quick_buttons_fragment.*

class CustomizeQuickButtonsFragment : BaseFragment() {
    override fun getFragmentView(): ViewGroup = customize_quick_buttons_fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.customize_quick_buttons_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        customize_quick_buttons_fragment_left.setOnClickListener {
            val chooseTimeFormatDialog = ChooseQuickButtonDialog(R.string.prefs_settings_key_quick_button_left_icon_id, R.drawable.ic_call)
            chooseTimeFormatDialog.setOnDismissListener(DialogInterface.OnDismissListener {
                setQuickButtonIcons()
            })
            chooseTimeFormatDialog.showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customize_quick_buttons_fragment_center.setOnClickListener {
            val chooseTimeFormatDialog = ChooseQuickButtonDialog(R.string.prefs_settings_key_quick_button_center_icon_id, R.drawable.ic_cog)
            chooseTimeFormatDialog.setOnDismissListener(DialogInterface.OnDismissListener {
                setQuickButtonIcons()
            })
            chooseTimeFormatDialog.showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
        customize_quick_buttons_fragment_right.setOnClickListener {
            val chooseTimeFormatDialog = ChooseQuickButtonDialog(R.string.prefs_settings_key_quick_button_right_icon_id, R.drawable.ic_photo_camera)
            chooseTimeFormatDialog.setOnDismissListener(DialogInterface.OnDismissListener {
                setQuickButtonIcons()
            })
            chooseTimeFormatDialog.showNow(childFragmentManager, "QUICK_BUTTON_CHOOSER")
        }
    }

    override fun onResume() {
        super.onResume()
        setQuickButtonIcons()
    }

    private fun setQuickButtonIcons() {
        customize_quick_buttons_fragment_left.setImageResource(getIcon(R.string.prefs_settings_key_quick_button_left_icon_id, R.drawable.ic_call))
        customize_quick_buttons_fragment_center.setImageResource(getIcon(R.string.prefs_settings_key_quick_button_center_icon_id, R.drawable.ic_cog))
        customize_quick_buttons_fragment_right.setImageResource(getIcon(R.string.prefs_settings_key_quick_button_right_icon_id, R.drawable.ic_photo_camera))
    }

    private fun getIcon(buttonPrefKey: Int, defaultIconId: Int): Int {
        return context?.getSharedPreferences(getString(R.string.prefs_settings), Context.MODE_PRIVATE)
                ?.getInt(getString(buttonPrefKey), defaultIconId) ?: defaultIconId
    }
}
