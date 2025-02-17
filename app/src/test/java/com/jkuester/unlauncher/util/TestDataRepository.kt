package com.jkuester.unlauncher.util

import androidx.lifecycle.Observer
import com.jkuester.unlauncher.datasource.DataRepository
import kotlinx.coroutines.Job

class TestDataRepository<T>(private var data: T) : DataRepository<T> {
    private val observers = mutableListOf<Observer<T>>()

    override fun observe(observer: Observer<T>) {
        observers.add(observer)
        observer.onChanged(data)
    }

    override fun get(): T = data

    override fun updateAsync(transform: (t: T) -> T): Job {
        data = transform(data)
        observers.forEach { it.onChanged(data) }
        return Job()
    }
}
