package com.parentalcare.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User document: users/{userId}
 * Created on sign-up. Holds the public profile only — no auth secrets.
 */
@Serializable
data class UserDoc(
    @SerialName("id") val userId: String,
    @SerialName("email") val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("role") val role: String, // PARENT or CHILD
    @SerialName("family_id") val familyId: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("is_email_verified") val isEmailVerified: Boolean = false,
)

/**
 * Family document: families/{familyId}
 * Created by the parent on first pairing.
 */
@Serializable
data class FamilyDoc(
    @SerialName("id") val familyId: String,
    @SerialName("name") val name: String,
    @SerialName("parent_user_id") val parentUserId: String,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("updated_at") val updatedAt: Long? = null,
    @SerialName("retention_hours") val retentionHours: Int = 24,
    @SerialName("biometric_lock_enabled") val biometricLockEnabled: Boolean = false,
)

/**
 * Family member: families/{familyId}/members/{memberId}
 * Mostly metadata about each linked account.
 */
@Serializable
data class MemberDoc(
    @SerialName("id") val memberId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("role") val role: String, // PARENT or CHILD
    @SerialName("display_name") val displayName: String,
    @SerialName("added_at") val addedAt: Long? = null,
)

/**
 * Child device: families/{familyId}/devices/{deviceId}
 * The authoritative record of a paired child device.
 */
@Serializable
data class DeviceDoc(
    @SerialName("id") val deviceId: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("owner_member_id") val ownerMemberId: String,
    @SerialName("child_display_name") val childDisplayName: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("android_version") val androidVersion: String,
    @SerialName("fcm_token") val fcmToken: String? = null,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("last_seen_at") val lastSeenAt: Long? = null,
    @SerialName("paired_at") val pairedAt: Long? = null,
    @SerialName("monitoring_active") val monitoringActive: Boolean = true,
    @SerialName("screenshot_count") val screenshotCount: Int = 0,
    @SerialName("request_count") val requestCount: Int = 0,
    @SerialName("last_screenshot_at") val lastScreenshotAt: Long? = null,
    @SerialName("retention_hours") val retentionHours: Int = 24,
)
