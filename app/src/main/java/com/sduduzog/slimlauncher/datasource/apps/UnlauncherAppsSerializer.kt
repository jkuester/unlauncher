package com.sduduzog.slimlauncher.datasource.apps

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.jkuester.unlauncher.datastore.UnlauncherApps
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for the [UnlauncherApps] object defined in quick_button_preferences.proto.
 */
object UnlauncherAppsSerializer : Serializer<UnlauncherApps> {
    override val defaultValue: UnlauncherApps = UnlauncherApps.getDefaultInstance()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun readFrom(input: InputStream): UnlauncherApps {
        try {
            return UnlauncherApps.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: UnlauncherApps, output: OutputStream) = t.writeTo(output)
}
