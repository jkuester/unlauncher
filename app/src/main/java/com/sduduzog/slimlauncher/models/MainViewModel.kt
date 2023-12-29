package com.sduduzog.slimlauncher.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sduduzog.slimlauncher.data.model.App
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var _baseRepository: Repository
) : ViewModel() {

    private var _apps: LiveData<List<HomeApp>> = _baseRepository.apps

    val apps: LiveData<List<HomeApp>>
        get() = _apps

    fun filterHomeApps(updatedApps: List<App>) {
        _baseRepository.apps.value
            .orEmpty()
            .filter { currentApp ->
                updatedApps.find { updatedApp ->
                    updatedApp.packageName == currentApp.packageName && updatedApp.activityName == currentApp.activityName
                } == null
            }.forEach {
                _baseRepository.remove(it)
            }
    }
}