package com.jkuester.unlauncher.datasource

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class AbstractDataRepository<T>(
    private val dataStore: DataStore<T>,
    private val lifecycleScope: CoroutineScope,
    getDefaultInstance: () -> T
) {
    private val dataFlow: Flow<T> =
        dataStore.data.catch {
            if (it is IOException) {
                Log.e("AbstractDataRepository", "Error reading data store.", it)
                emit(getDefaultInstance())
            } else {
                throw it
            }
        }

    fun observe(owner: LifecycleOwner, observer: Observer<T>) = dataFlow
        .asLiveData()
        .observe(owner, observer)

    fun get(): T = runBlocking { dataFlow.first() }

    fun updateAsync(transform: suspend (t: T) -> T) = lifecycleScope.launch(Dispatchers.IO) {
        dataStore.updateData(transform)
    }
}

abstract class AbstractDataSerializer<T>(getDefaultInstance: () -> T, private val parseFrom: (InputStream) -> T) :
    Serializer<T> where T : GeneratedMessageLite<T, *> {
    override val defaultValue = getDefaultInstance()

    override suspend fun readFrom(input: InputStream): T = try {
        parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", exception)
    }

    override suspend fun writeTo(t: T, output: OutputStream) = t.writeTo(output)
}
