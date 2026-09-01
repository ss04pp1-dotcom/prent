package com.parentalcare.core.common.model

/**
 * Multi-tenant ownership triple.
 * Embedded into every Firestore document and every Storage metadata blob.
 */
data class Ownership(
    val familyId: String,
    val parentUserId: String,
    val childDeviceId: String,
) {
    fun isValid(): Boolean =
        familyId.isNotBlank() && parentUserId.isNotBlank() && childDeviceId.isNotBlank()

    /** Storage path segment: families/{familyId}/screenshots/{childDeviceId}/{screenshotId}.enc */
    fun storagePath(screenshotId: String): String =
        "families/$familyId/screenshots/$childDeviceId/$screenshotId.enc"
}
