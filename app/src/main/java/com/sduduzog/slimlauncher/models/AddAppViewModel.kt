package com.sduduzog.slimlauncher.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sduduzog.slimlauncher.data.model.App
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class AddAppViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private var filterQuery = ""
    private val regex = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/? ]")
    private val installedApps = mutableListOf<App>()
    private val homeApps = mutableListOf<App>()
    private val homeAppsObserver = Observer<List<HomeApp>> {
        this.homeApps.clear()
        it.orEmpty().forEach { item -> this.homeApps.add(App.from(item)) }
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
        val filteredApps = installedApps.filterNot { homeApps.contains(it) }
        this.apps.postValue(
            filteredApps.filter {
                regex.replace(it.appName, "").contains(filterQuery, ignoreCase = true)
            }
        )
    }

    fun setInstalledApps(apps: List<App>) {
        this.filterQuery = ""
        this.installedApps.clear()
        this.installedApps.addAll(apps)
    }

    fun addAppToHomeScreen(app: App) {
        val index = homeApps.size
        viewModelScope.launch(Dispatchers.IO) {
            repository.add(HomeApp.from(app, index))
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.apps.removeObserver(homeAppsObserver)
    }
}
