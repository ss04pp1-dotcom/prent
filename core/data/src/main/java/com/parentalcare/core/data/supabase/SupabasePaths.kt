package com.parentalcare.core.data.supabase

/**
 * Supabase table and storage path helpers. Centralizes the schema so that
 * RLS policies stay in sync with app-side queries.
 */
object SupabasePaths {

    // Table names
    const val TABLE_USERS = "users"
    const val TABLE_FAMILIES = "families"
    const val TABLE_MEMBERS = "members"
    const val TABLE_DEVICES = "devices"
    const val TABLE_SCREENSHOT_REQUESTS = "screenshot_requests"
    const val TABLE_SCREENSHOTS = "screenshots"
    const val TABLE_PAIRING_TOKENS = "pairing_tokens"
    const val TABLE_ACTIVITY_LOG = "activity_log"

    // Storage bucket
    const val STORAGE_BUCKET_SCREENSHOTS = "screenshots"

    // Column names (snake_case for Postgres)
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val FAMILY_ID = "family_id"
        const val DEVICE_ID = "device_id"
        const val PARENT_USER_ID = "parent_user_id"
        const val CHILD_DEVICE_ID = "child_device_id"
        const val OWNER_MEMBER_ID = "owner_member_id"
        const val REQUEST_ID = "request_id"
        const val SCREENSHOT_ID = "screenshot_id"
        const val TOKEN_ID = "token_id"
        const val STATUS = "status"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
        const val EXPIRES_AT = "expires_at"
        const val COMPLETED_AT = "completed_at"
        const val CAPTURED_AT = "captured_at"
        const val DELIVERED_AT = "delivered_at"
        const val VIEWED_AT = "viewed_at"
        const val DELETED_AT = "deleted_at"
        const val RETENTION_EXPIRES_AT = "retention_expires_at"
        const val IS_UNREAD = "is_unread"
        const val IS_ONLINE = "is_online"
        const val IS_CONSUMED = "is_consumed"
        const val IS_MONITORING_ACTIVE = "monitoring_active"
        const val PAIRED_AT = "paired_at"
        const val LAST_SEEN_AT = "last_seen_at"
        const val LAST_SCREENSHOT_AT = "last_screenshot_at"
        const val SCREENSHOT_COUNT = "screenshot_count"
        const val REQUEST_COUNT = "request_count"
        const val CHILD_DISPLAY_NAME = "child_display_name"
        const val DEVICE_NAME = "device_name"
        const val DEVICE_MODEL = "device_model"
        const val ANDROID_VERSION = "android_version"
        const val FCM_TOKEN = "fcm_token"
        const val NONCE = "nonce"
        const val DELAY_SECONDS = "delay_seconds"
        const val FAILURE_REASON = "failure_reason"
        const val CANCELLATION_REASON = "cancellation_reason"
        const val CONSUMED_BY_DEVICE_ID = "consumed_by_device_id"
        const val STORAGE_PATH = "storage_path"
        const val IV = "iv"
        const val WRAPPED_KEY = "wrapped_key"
        const val THUMBNAIL_BASE64 = "thumbnail_base64"
        const val MIME_TYPE = "mime_type"
        const val WIDTH_PX = "width_px"
        const val HEIGHT_PX = "height_px"
        const val SIZE_BYTES = "size_bytes"
        const val ENCRYPTED_PAYLOAD_BASE64 = "encrypted_payload_base64"
        const val RETENTION_HOURS = "retention_hours"
        const val BIOMETRIC_LOCK_ENABLED = "biometric_lock_enabled"
        const val EMAIL = "email"
        const val DISPLAY_NAME = "display_name"
        const val ROLE = "role"
        const val PARENT_EMAIL = "parent_email"
        const val PARENT_DISPLAY_NAME = "parent_display_name"
        const val NAME = "name"
        const val PHOTO_URL = "photo_url"
        const val IS_EMAIL_VERIFIED = "is_email_verified"
    }
}