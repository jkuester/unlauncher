package com.sduduzog.slimlauncher.datasource.quickbuttonprefs

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.jkuester.unlauncher.datastore.QuickButtonPreferences
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for the [QuickButtonPreferences] object defined in quick_button_preferences.proto.
 */
object QuickButtonPreferencesSerializer : Serializer<QuickButtonPreferences> {
    override val defaultValue: QuickButtonPreferences = QuickButtonPreferences.getDefaultInstance()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun readFrom(input: InputStream): QuickButtonPreferences {
        try {
            return QuickButtonPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: QuickButtonPreferences, output: OutputStream) =
        t.writeTo(output)
}
