package com.sduduzog.slimlauncher.models

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.sduduzog.slimlauncher.data.BaseDao
import javax.inject.Singleton

class Repository(private val baseDao: BaseDao) {

    private val _apps = baseDao.apps

    val apps: LiveData<List<HomeApp>>
        get() = _apps

    fun add(app: HomeApp) {
        AddAppAsyncTask(baseDao).execute(app)
    }

    fun update(vararg list : HomeApp) {
        UpdateAppAsyncTask(baseDao).execute(*list)
    }

    fun remove(app: HomeApp) {
        RemoveAppAsyncTask(baseDao).execute(app)
    }

    fun clearTable(){
        ClearTableAsyncTask(baseDao).execute()
    }

    private class AddAppAsyncTask(private val mAsyncTaskDao: BaseDao) : AsyncTask<HomeApp, Void, Void>() {

        override fun doInBackground(vararg params: HomeApp): Void? {
            mAsyncTaskDao.add(params[0])
            return null
        }
    }

    private class UpdateAppAsyncTask(private val mAsyncTaskDao: BaseDao) : AsyncTask<HomeApp, Void, Void>() {

        override fun doInBackground(vararg params: HomeApp): Void? {
            mAsyncTaskDao.update(*params)
            return null
        }
    }

    private class RemoveAppAsyncTask(private val mAsyncTaskDao: BaseDao) : AsyncTask<HomeApp, Void, Void>() {

        override fun doInBackground(vararg params: HomeApp): Void? {
            mAsyncTaskDao.remove(params[0])
            return null
        }
    }

    private class ClearTableAsyncTask(private val mAsyncTaskDao: BaseDao) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            mAsyncTaskDao.clearTable()
            return null
        }
    }
}
