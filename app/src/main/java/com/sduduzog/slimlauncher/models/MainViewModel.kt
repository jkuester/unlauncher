package com.sduduzog.slimlauncher.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var _baseRepository: Repository
) : ViewModel() {

    private var _apps: LiveData<List<HomeApp>> = _baseRepository.apps

    val apps: LiveData<List<HomeApp>>
        get() = _apps

    fun updateHomeApps(apps: List<HomeApp>) {
        _baseRepository.update(*apps.toTypedArray())
    }
}