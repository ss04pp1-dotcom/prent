package com.parentalcare.core.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

object SupabaseTimestampSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SupabaseTimestamp", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Long {
        val string = decoder.decodeString()
        var s = string
        if (s.endsWith("+00")) {
            s = s.substring(0, s.length - 3) + "Z"
        }
        return try {
            Instant.parse(s).toEpochMilli()
        } catch (e: Exception) {
            string.toLongOrNull() ?: 0L
        }
    }

    override fun serialize(encoder: Encoder, value: Long) {
        val string = Instant.ofEpochMilli(value).toString()
        encoder.encodeString(string)
    }
}
