package com.parentalcare.core.common.model

/**
 * Server-side timestamped envelope for safe error reporting.
 * Used by ActivityLog documents in Firestore.
 */
data class ActivityEvent(
    val eventId: String = "",
    val familyId: String = "",
    val actorId: String = "",
    val actorType: ActorType = ActorType.PARENT,
    val type: EventType,
    val targetDeviceId: String? = null,
    val requestId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    /** Safe, redacted message — no PII / secrets. */
    val message: String,
)

enum class ActorType { PARENT, CHILD, SYSTEM }

enum class EventType {
    PAIRING_CREATED,
    PAIRING_REDEEMED,
    DEVICE_ONLINE,
    DEVICE_OFFLINE,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    REQUEST_ACCEPTED,
    REQUEST_REJECTED,
    REQUEST_EXPIRED,
    SCREENSHOT_CAPTURED,
    SCREENSHOT_UPLOADED,
    SCREENSHOT_DELIVERED,
    SCREENSHOT_VIEWED,
    SCREENSHOT_DELETED,
    MONITORING_STARTED,
    MONITORING_STOPPED,
    DEVICE_UNPAIRED,
    AUTH_EXPIRED,
}
