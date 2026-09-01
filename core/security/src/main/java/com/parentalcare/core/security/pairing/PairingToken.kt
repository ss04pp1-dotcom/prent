package com.parentalcare.core.security.pairing

import com.parentalcare.core.common.util.SecureTokenGenerator
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class PairingToken(
    val tokenId: String,
    val opaque: String,
    val familyId: String,
    val parentUserId: String,
    val parentDisplayName: String,
    val parentEmail: String,
    val parentPublicKey: String,
    val createdAt: Long,
    val expiresAt: Long,
    val nonce: String,
    val isConsumed: Boolean = false,
    val consumedByDeviceId: String? = null,
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt

    fun verify(other: PairingToken): Boolean {
        return constantTimeEquals(tokenId, other.tokenId)
            && constantTimeEquals(opaque, other.opaque)
            && constantTimeEquals(nonce, other.nonce)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}

class PairingTokenFactory {
    fun issue(
        familyId: String,
        parentUserId: String,
        parentDisplayName: String,
        parentEmail: String,
        parentPublicKey: String,
        ttlSeconds: Long = 120,
        now: Long = System.currentTimeMillis(),
    ): PairingToken {
        val tokenId = SecureTokenGenerator.generateOpaqueToken()
        val opaque = SecureTokenGenerator.generateOpaqueToken()
        val nonce = SecureTokenGenerator.generateNonce()
        return PairingToken(
            tokenId = tokenId,
            opaque = opaque,
            familyId = familyId,
            parentUserId = parentUserId,
            parentDisplayName = parentDisplayName,
            parentEmail = parentEmail,
            parentPublicKey = parentPublicKey,
            createdAt = now,
            expiresAt = now + ttlSeconds * 1000,
            nonce = nonce,
        )
    }

    fun hashKey(token: PairingToken): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(token.opaque.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
