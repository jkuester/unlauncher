package com.sduduzog.slimlauncher.datasource.coreprefs

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.jkuester.unlauncher.datastore.CorePreferences
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for the [CorePreferences] object defined in core_preferences.proto.
 */
object CorePreferencesSerializer : Serializer<CorePreferences> {
    override val defaultValue: CorePreferences = CorePreferences.getDefaultInstance()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun readFrom(input: InputStream): CorePreferences {
        try {
            return CorePreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: CorePreferences, output: OutputStream) =
        t.writeTo(output)
}