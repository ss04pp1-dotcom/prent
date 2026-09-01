package com.parentalcare.core.data.model

import kotlinx.serialization.Serializable

/**
 * User document: users/{userId}
 * Created on sign-up. Holds the public profile only — no auth secrets.
 */
@Serializable
data class UserDoc(
    val userId: String,
    val email: String,
    val displayName: String,
    val role: String, // PARENT or CHILD
    val familyId: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isEmailVerified: Boolean = false,
)

/**
 * Family document: families/{familyId}
 * Created by the parent on first pairing.
 */
@Serializable
data class FamilyDoc(
    val familyId: String,
    val name: String,
    val parentUserId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retentionHours: Int = 24,
    val biometricLockEnabled: Boolean = false,
)

/**
 * Family member: families/{familyId}/members/{memberId}
 * Mostly metadata about each linked account.
 */
@Serializable
data class MemberDoc(
    val memberId: String,
    val userId: String,
    val familyId: String,
    val role: String, // PARENT or CHILD
    val displayName: String,
    val addedAt: Long = System.currentTimeMillis(),
)

/**
 * Child device: families/{familyId}/devices/{deviceId}
 * The authoritative record of a paired child device.
 */
@Serializable
data class DeviceDoc(
    val deviceId: String,
    val familyId: String,
    val ownerMemberId: String,
    val childDisplayName: String,
    val deviceName: String,
    val deviceModel: String,
    val androidVersion: String,
    val fcmToken: String? = null,
    val isOnline: Boolean = false,
    val lastSeenAt: Long = System.currentTimeMillis(),
    val pairedAt: Long = System.currentTimeMillis(),
    val monitoringActive: Boolean = true,
    val screenshotCount: Int = 0,
    val requestCount: Int = 0,
    val lastScreenshotAt: Long? = null,
    val retentionHours: Int = 24,
)
