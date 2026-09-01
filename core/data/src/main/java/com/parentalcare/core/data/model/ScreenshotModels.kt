package com.parentalcare.core.data.model

import kotlinx.serialization.Serializable

/**
 * Screenshot request: families/{familyId}/screenshotRequests/{requestId}
 *
 * Lifecycle:
 *   REQUESTED → PROCESSING → UPLOADED → DELIVERED → DELETED
 *                                       ↘ EXPIRED
 *                                       ↘ FAILED
 *
 * The child device can ONLY update status to PROCESSING / UPLOADED / FAILED.
 * The parent app can ONLY update status to DELIVERED / CANCELLED.
 * Status transitions are enforced in `firestore.rules`.
 */
@Serializable
data class ScreenshotRequestDoc(
    val requestId: String,
    val familyId: String,
    val parentUserId: String,
    val childDeviceId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val nonce: String,
    val status: String = "REQUESTED",
    val delaySeconds: Int = 0,
    val completedAt: Long? = null,
    val failureReason: String? = null,
    val cancellationReason: String? = null,
) {
    /**
     * True once the request's expiry has passed. Used by the child app
     * to drop stale FCM pushes and refuse to honour late captures.
     * Mirrors [com.parentalcare.core.security.pairing.ScreenshotRequest.isExpired].
     */
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
}

/**
 * Screenshot metadata: families/{familyId}/screenshots/{screenshotId}
 *
 * The encrypted image bytes live in Cloud Storage at the path returned by
 * [StoragePaths.screenshot]. The Firestore document carries only metadata +
 * the encrypted key material needed to decrypt (never the screenshot bytes
 * themselves).
 */
@Serializable
data class ScreenshotDoc(
    val screenshotId: String,
    val familyId: String,
    val parentUserId: String,
    val childDeviceId: String,
    val requestId: String,
    val storagePath: String,
    /** Base64 IV used by AES-GCM (transported alongside ciphertext). */
    val iv: String,
    /** Wrapped content key — base64("iv:ciphertext") produced by KeystoreManager.wrap(). */
    val wrappedKey: String,
    val thumbnailBase64: String? = null,
    val mimeType: String = "image/jpeg",
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val sizeBytes: Long = 0L,
    val capturedAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null,
    val viewedAt: Long? = null,
    val deletedAt: Long? = null,
    val retentionExpiresAt: Long,
    val isUnread: Boolean = true,
    val encryptedPayloadBase64: String? = null,
)
