package com.parentalcare.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Common FCM service — the parent and child apps each register their own
 * concrete subclass because each app needs a unique manifest entry pointing
 * at its own subclass.
 *
 * Inheritance-only base class here; subclasses override [onDataMessage] to
 * handle the `type` field.
 *
 * NEVER include screenshot image bytes, encryption keys, or download URLs
 * containing credentials in the push payload — push notifications are not
 * end-to-end encrypted. The data payload only carries opaque IDs:
 *   { type, requestId, familyId, childDeviceId, screenshotId, ts }
 */
abstract class BaseFcmService : FirebaseMessagingService() {

    final override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: return
        Timber.tag(TAG).i("FCM type=%s ts=%s", type, data["ts"])
        // Only allow safe opaques through — never raw content.
        val safe = SafePayload.fromData(data) ?: return
        onDataMessage(safe)
    }

    final override fun onNewToken(token: String) {
        Timber.tag(TAG).i("FCM token refreshed: prefix=%s", token.take(8))
        onTokenRefresh(token)
    }

    abstract fun onDataMessage(payload: SafePayload)
    abstract fun onTokenRefresh(token: String)

    companion object { const val TAG = "PC.FCM" }
}

/**
 * Whitelisted push payload fields. Anything else in the data map is dropped.
 */
data class SafePayload(
    val type: String,
    val requestId: String? = null,
    val familyId: String? = null,
    val childDeviceId: String? = null,
    val screenshotId: String? = null,
    val ts: Long = System.currentTimeMillis(),
) {
    companion object {
        fun fromData(d: Map<String, String>): SafePayload? {
            val type = d["type"] ?: return null
            return SafePayload(
                type = type,
                requestId = d["requestId"],
                familyId = d["familyId"],
                childDeviceId = d["childDeviceId"],
                screenshotId = d["screenshotId"],
                ts = d["ts"]?.toLongOrNull() ?: System.currentTimeMillis(),
            )
        }
    }
}
