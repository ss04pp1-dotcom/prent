package com.parentalcare.core.data.model

import kotlinx.serialization.SerialName
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
    @SerialName("id") val requestId: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("parent_user_id") val parentUserId: String,
    @SerialName("child_device_id") val childDeviceId: String,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("nonce") val nonce: String,
    @SerialName("status") val status: String = "REQUESTED",
    @SerialName("delay_seconds") val delaySeconds: Int = 0,
    @SerialName("completed_at") val completedAt: Long? = null,
    @SerialName("failure_reason") val failureReason: String? = null,
    @SerialName("cancellation_reason") val cancellationReason: String? = null,
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
    @SerialName("id") val screenshotId: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("parent_user_id") val parentUserId: String,
    @SerialName("child_device_id") val childDeviceId: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("storage_path") val storagePath: String,
    /** Base64 IV used by AES-GCM (transported alongside ciphertext). */
    @SerialName("iv") val iv: String,
    /** Wrapped content key — base64("iv:ciphertext") produced by KeystoreManager.wrap(). */
    @SerialName("wrapped_key") val wrappedKey: String,
    @SerialName("thumbnail_base64") val thumbnailBase64: String? = null,
    @SerialName("mime_type") val mimeType: String = "image/jpeg",
    @SerialName("width_px") val widthPx: Int = 0,
    @SerialName("height_px") val heightPx: Int = 0,
    @SerialName("size_bytes") val sizeBytes: Long = 0L,
    @SerialName("captured_at") val capturedAt: Long? = null,
    @SerialName("delivered_at") val deliveredAt: Long? = null,
    @SerialName("viewed_at") val viewedAt: Long? = null,
    @SerialName("deleted_at") val deletedAt: Long? = null,
    @SerialName("retention_expires_at") val retentionExpiresAt: Long,
    @SerialName("is_unread") val isUnread: Boolean = true,
    @SerialName("encrypted_payload_base64") val encryptedPayloadBase64: String? = null,
)
