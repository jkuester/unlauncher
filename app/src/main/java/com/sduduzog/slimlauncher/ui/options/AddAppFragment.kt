package com.sduduzog.slimlauncher.ui.options

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.sduduzog.slimlauncher.R
import com.sduduzog.slimlauncher.adapters.AddAppAdapter
import com.sduduzog.slimlauncher.data.model.App
import com.sduduzog.slimlauncher.databinding.AddAppFragmentBinding
import com.sduduzog.slimlauncher.models.AddAppViewModel
import com.sduduzog.slimlauncher.utils.BaseFragment
import com.sduduzog.slimlauncher.utils.OnAppClickedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddAppFragment : BaseFragment(), OnAppClickedListener {

    override fun getFragmentView(): ViewGroup = AddAppFragmentBinding.bind(requireView()).addAppFragment

    private  val viewModel: AddAppViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_app_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = AddAppAdapter(this)

        val addAppFragment = AddAppFragmentBinding.bind(requireView())
        addAppFragment.addAppFragmentList.adapter = adapter

        viewModel.apps.observe(viewLifecycleOwner, {
            it?.let { apps ->
                adapter.setItems(apps)
                addAppFragment.addAppFragmentProgressBar.visibility = View.GONE
            } ?: run {
                addAppFragment.addAppFragmentProgressBar.visibility = View.VISIBLE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.setInstalledApps(getInstalledApps())
        viewModel.filterApps("")
        val addAppFragment = AddAppFragmentBinding.bind(requireView())
        addAppFragment.addAppFragmentEditText.addTextChangedListener(onTextChangeListener)
    }

    override fun onPause() {
        super.onPause()
        val addAppFragment = AddAppFragmentBinding.bind(requireView())
        addAppFragment.addAppFragmentEditText.removeTextChangedListener(onTextChangeListener)
    }

    private val onTextChangeListener: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            // Do nothing
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.filterApps(s.toString())
        }
    }

    override fun onAppClicked(app: App) {
        viewModel.addAppToHomeScreen(app)
        Navigation.findNavController(AddAppFragmentBinding.bind(requireView()).addAppFragment).popBackStack()
    }
}