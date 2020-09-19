package com.sduduzog.slimlauncher.models

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.model.App

class AddAppViewModel @ViewModelInject constructor (baseDao: BaseDao) : ViewModel() {
    private val repository = Repository(baseDao)
    private var filterQuery = ""
    private val regex = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/? ]")
    private val _installedApps = mutableListOf<App>()
    private val _homeApps = mutableListOf<App>()
    private val homeAppsObserver = Observer<List<HomeApp>> {
        this._homeApps.clear()
        it.orEmpty().forEach { item -> this._homeApps.add(App.from(item)) }
        if (it !== null) updateDisplayedApps()
    }
    val apps = MutableLiveData<List<App>>()

    init {
        repository.apps.observeForever(homeAppsObserver)
    }

    fun filterApps(query: String = "") {
        this.filterQuery = regex.replace(query, "")
        this.updateDisplayedApps()
    }

    private fun updateDisplayedApps() {
        val filteredApps = _installedApps.filterNot { _homeApps.contains(it) }
        this.apps.postValue(filteredApps.filter { regex.replace(it.appName, "").contains(filterQuery, ignoreCase = true) })
    }

    fun setInstalledApps(apps: List<App>) {
        this.filterQuery = ""
        this._installedApps.clear()
        this._installedApps.addAll(apps)
    }

    fun addAppToHomeScreen(app: App) {
        val index = _homeApps.size
        repository.add(HomeApp.from(app, index))
    }

    override fun onCleared() {
        super.onCleared()
        repository.apps.removeObserver(homeAppsObserver)
    }
}