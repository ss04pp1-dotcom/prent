package com.parentalcare.parent.pairing

import com.parentalcare.core.data.pairing.PairingRepository
import com.parentalcare.core.security.pairing.PairingSerializer
import com.parentalcare.core.security.pairing.PairingToken
import com.parentalcare.core.security.keystore.KeystoreManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingIssuer @Inject constructor(
    private val pairingRepo: PairingRepository,
    private val serializer: PairingSerializer,
    private val keystore: KeystoreManager,
) {
    suspend fun issueForQr(
        familyId: String,
        parentDisplayName: String,
        parentEmail: String,
    ): Pair<String, PairingToken> {
        val parentPublicKey = keystore.getParentPublicKeyBase64()
        val token = pairingRepo
            .issuePairingToken(familyId, parentDisplayName, parentEmail, parentPublicKey)
            .getOrNull()
            ?: throw IllegalStateException("failed to issue pairing token")
        return serializer.encode(token) to token
    }
}
