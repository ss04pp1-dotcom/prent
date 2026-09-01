package com.parentalcare.core.security.pairing

import com.parentalcare.core.common.util.SecureTokenGenerator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class PairingToken(
    @SerialName("token_id") val tokenId: String,
    @SerialName("opaque") val opaque: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("parent_user_id") val parentUserId: String,
    @SerialName("parent_display_name") val parentDisplayName: String,
    @SerialName("parent_email") val parentEmail: String,
    @SerialName("parent_public_key") val parentPublicKey: String? = null,
    @SerialName("parent_encryption_public_key") val parentEncryptionPublicKey: String? = null,
    @Serializable(with = com.parentalcare.core.security.model.SupabaseTimestampSerializer::class)
    @SerialName("created_at") val createdAt: Long,
    @Serializable(with = com.parentalcare.core.security.model.SupabaseTimestampSerializer::class)
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("nonce") val nonce: String,
    @SerialName("is_consumed") val isConsumed: Boolean = false,
    @SerialName("consumed_by_device_id") val consumedByDeviceId: String? = null,
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
        parentPublicKey: String? = null,
        parentEncryptionPublicKey: String? = null,
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
            parentEncryptionPublicKey = parentEncryptionPublicKey,
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
