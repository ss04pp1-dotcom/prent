package com.parentalcare.core.security.pairing

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Base64

/**
 * Serializes a [PairingToken] to a QR-scannable string and back.
 *
 * Format: base64url(JSON) — no permanent secrets, only short-lived tokens.
 * The child scans → decodes → calls Firestore to redeem.
 */
class PairingSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun encode(token: PairingToken): String {
        val payload = json.encodeToString(token).toByteArray(Charsets.UTF_8)
        return PREFIX + Base64.encodeToString(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun decode(scanned: String): com.parentalcare.core.common.result.Result<PairingToken> {
    return try {
        require(scanned.startsWith(PREFIX)) { "Not a Parental Care pairing code" }
        val raw = scanned.removePrefix(PREFIX)
        val payload = Base64.decode(raw, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        com.parentalcare.core.common.result.Result.Success(json.decodeFromString<PairingToken>(payload.decodeToString()))
    } catch(e: Exception) {
        com.parentalcare.core.common.result.Result.Failure(com.parentalcare.core.common.result.AppError.from(e))
    }
}

    companion object {
        // Scheme prefix — child app rejects QR codes that don't start with this.
        const val PREFIX = "PC1:"
    }
}
