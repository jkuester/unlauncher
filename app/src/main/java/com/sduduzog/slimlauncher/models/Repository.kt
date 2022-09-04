package com.sduduzog.slimlauncher.models

import androidx.lifecycle.LiveData
import com.sduduzog.slimlauncher.data.BaseDao

class Repository(private val baseDao: BaseDao) {

    private val _apps = baseDao.apps

    val apps: LiveData<List<HomeApp>>
        get() = _apps

    fun add(app: HomeApp) {
        baseDao.add(app)
    }

    fun update(vararg list : HomeApp) {
        baseDao.update(*list)
    }

    fun remove(app: HomeApp) {
        baseDao.remove(app)
    }

    fun clearTable(){
        baseDao.clearTable()
    }
}
