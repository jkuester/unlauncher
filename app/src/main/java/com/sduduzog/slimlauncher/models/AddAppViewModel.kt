package com.sduduzog.slimlauncher.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.model.App
import java.util.regex.Pattern
import javax.inject.Inject

class AddAppViewModel @Inject constructor (baseDao: BaseDao) : ViewModel() {
    private val repository = Repository(baseDao)
    private var filterQuery = Pattern.compile(".*")
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
        this.filterQuery = Pattern.compile(".*" + Pattern.quote(query) + ".*",
                Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
        this.updateDisplayedApps()
    }

    private fun updateDisplayedApps() {
        val filteredApps = _installedApps.filterNot { _homeApps.contains(it) }
        this.apps.postValue(filteredApps.filter { filterQuery.matcher(it.appName).matches() })
    }

    fun setInstalledApps(apps: List<App>) {
        this.filterQuery = Pattern.compile(".*")
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