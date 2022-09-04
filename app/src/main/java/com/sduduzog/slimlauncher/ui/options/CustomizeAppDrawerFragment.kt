package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.datasource.UnlauncherDataSource
import com.sduduzog.slimlauncher.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.customize_app_drawer_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeAppDrawerFragment : BaseFragment() {
    @Inject
    lateinit var unlauncherDataSource: UnlauncherDataSource

    override fun getFragmentView(): ViewGroup = customize_app_drawer_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.customize_app_drawer_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customize_app_drawer_fragment_visible_apps
            .setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_customiseAppDrawerFragment_to_customiseAppDrawerAppListFragment))

        setupKeyboardSwitch()
    }

    private fun setupKeyboardSwitch() {
        val prefsRepo = unlauncherDataSource.corePreferencesRepo
        customize_app_drawer_open_keyboard_switch.setOnCheckedChangeListener { _, checked ->
            prefsRepo.updateActivateKeyboardInDrawer(checked)
        }
        prefsRepo.liveData().observe(viewLifecycleOwner) {
            customize_app_drawer_open_keyboard_switch.isChecked = it.activateKeyboardInDrawer
        }
    }
}
