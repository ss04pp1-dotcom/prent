package com.parentalcare.core.data.device

import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.common.util.Redactor
import com.parentalcare.core.data.model.DeviceDoc
import com.parentalcare.core.data.supabase.SupabasePaths
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonPrimitive

@Singleton
class DeviceRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val table = SupabasePaths.TABLE_DEVICES


    suspend fun updateFcmToken(deviceId: String, familyId: String, token: String): Result<Unit> = resultOf {
        supabase.postgrest[table].update(
            mapOf(
                SupabasePaths.Columns.FCM_TOKEN to token,
                SupabasePaths.Columns.LAST_SEEN_AT to System.currentTimeMillis()
            )
        ) {
            filter {
                eq(SupabasePaths.Columns.DEVICE_ID, deviceId)
                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
            }
        }
    }

    suspend fun setOnline(deviceId: String, familyId: String, online: Boolean): Result<Unit> = resultOf {
        supabase.postgrest[table].update(
            mapOf(
                SupabasePaths.Columns.IS_ONLINE to online,
                SupabasePaths.Columns.LAST_SEEN_AT to System.currentTimeMillis()
            )
        ) {
            filter {
                eq(SupabasePaths.Columns.DEVICE_ID, deviceId)
                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
            }
        }
    }

    suspend fun setMonitoringActive(deviceId: String, familyId: String, active: Boolean): Result<Unit> = resultOf {
        supabase.postgrest[table].update(
            mapOf(SupabasePaths.Columns.IS_MONITORING_ACTIVE to active)
        ) {
            filter {
                eq(SupabasePaths.Columns.DEVICE_ID, deviceId)
                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
            }
        }
    }

    suspend fun incrementCounters(
        deviceId: String,
        familyId: String,
        screenshot: Boolean = false,
        request: Boolean = false,
        lastScreenshotAt: Long? = null,
    ): Result<Unit> = resultOf {
        val device = getCurrentDevice(deviceId, familyId).getOrNull()
        if (device != null) {
            val updates = mutableMapOf<String, Any>()
            if (screenshot) updates[SupabasePaths.Columns.SCREENSHOT_COUNT] = device.screenshotCount + 1
            if (request) updates[SupabasePaths.Columns.REQUEST_COUNT] = device.requestCount + 1
            if (lastScreenshotAt != null) updates[SupabasePaths.Columns.LAST_SCREENSHOT_AT] = lastScreenshotAt
            
            if (updates.isNotEmpty()) {
                supabase.postgrest[table].update(updates) {
                    filter {
                        eq(SupabasePaths.Columns.DEVICE_ID, deviceId)
                        eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                    }
                }
            }
        }
    }

    suspend fun listDevices(familyId: String): Result<List<DeviceDoc>> = resultOf {
        supabase.postgrest[table]
            .select { filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) } }
            .decodeList<DeviceDoc>()
    }

    suspend fun removeDevice(familyId: String, deviceId: String): Result<Unit> = resultOf {
        supabase.postgrest[table].delete {
            filter {
                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                eq(SupabasePaths.Columns.DEVICE_ID, deviceId)
            }
        }
    }

    suspend fun getCurrentDevice(deviceId: String, familyId: String): Result<DeviceDoc?> = resultOf {
        supabase.postgrest[table]
            .select {
                filter {
                    eq(SupabasePaths.Columns.DEVICE_ID, deviceId)
                    eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                }
            }
            .decodeSingleOrNull<DeviceDoc>()
    }

    private companion object { const val TAG = "PC.SupabaseDeviceRepo" }
}
