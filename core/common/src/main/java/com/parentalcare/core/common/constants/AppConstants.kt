package com.parentalcare.core.common.constants

/**
 * Global application constants. No secrets, tokens, or PII here.
 */
object AppConstants {

    /** Firestore root collection paths. */
    object Paths {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_FAMILIES = "families"
        const val COLLECTION_MEMBERS = "members"
        const val COLLECTION_DEVICES = "devices"
        const val COLLECTION_SCREENSHOT_REQUESTS = "screenshotRequests"
        const val COLLECTION_SCREENSHOTS = "screenshots"
        const val COLLECTION_PAIRING_TOKENS = "pairingTokens"
        const val COLLECTION_ACTIVITY_LOG = "activityLog"
    }

    /** Screenshot request lifecycle. */
    object Request {
        /** Default expiration for a parent-issued request. */
        const val DEFAULT_EXPIRATION_MINUTES = 5L

        /** Maximum expiration allowed (replay-attack mitigation). */
        const val MAX_EXPIRATION_MINUTES = 30L

        /** Status enum values persisted to Firestore. */
        const val STATUS_REQUESTED = "REQUESTED"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_UPLOADED = "UPLOADED"
        const val STATUS_DELIVERED = "DELIVERED"
        const val STATUS_EXPIRED = "EXPIRED"
        const val STATUS_CANCELLED = "CANCELLED"
        const val STATUS_FAILED = "FAILED"
    }

    /** Screenshot retention defaults. */
    object Retention {
        const val DEFAULT_HOURS = 24
        val ALLOWED_HOURS = listOf(1, 6, 24, 168) // 1h, 6h, 24h, 7d
    }

    /** Image compression defaults. */
    object Image {
        const val MAX_WIDTH_PX = 1080
        const val MAX_HEIGHT_PX = 1920
        const val JPEG_QUALITY = 80
        const val WEBP_QUALITY = 82
        const val MAX_FILE_BYTES = 600 * 1024 // 600 KB hard limit
    }

    /** Pairing flow. */
    object Pairing {
        const val TOKEN_TTL_SECONDS = 120L
        const val TOKEN_LENGTH = 32
    }

    /** Notification channels. */
    object Channels {
        const val CHANNEL_MONITORING = "channel_monitoring"
        const val CHANNEL_SCREENSHOT_REQUEST = "channel_screenshot_request"
        const val CHANNEL_SCREENSHOT_RECEIVED = "channel_screenshot_received"
        const val CHANNEL_LOW_PRIORITY = "channel_low"
    }

    /** Foreground service IDs. */
    object Services {
        const val CAPTURE_SERVICE_ID = 1001
        const val MONITORING_SERVICE_ID = 1002
    }

    /** Notification IDs. */
    object Notifications {
        const val SCREENSHOT_REQUEST = 2001
        const val REQUEST_TIMEOUT = 2002
        const val SCREENSHOT_RECEIVED = 9002
        const val DEVICE_STATUS = 9003
    }
}
