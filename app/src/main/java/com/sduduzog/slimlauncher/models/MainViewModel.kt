package com.sduduzog.slimlauncher.models

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.model.App

class MainViewModel @ViewModelInject constructor(baseDao: BaseDao) : ViewModel() {

    private val _baseRepository = Repository(baseDao)

    private var _apps: LiveData<List<HomeApp>>

    init {
        _apps = _baseRepository.apps
    }

    val apps: LiveData<List<HomeApp>>
        get() = _apps

    fun add(app: App) {
        val index = _apps.value!!.size
        _baseRepository.add(HomeApp.from(app, index))
    }
}