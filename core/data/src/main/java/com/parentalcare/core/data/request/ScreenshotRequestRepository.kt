package com.parentalcare.core.data.request

import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.common.util.Redactor
import com.parentalcare.core.data.model.ScreenshotRequestDoc
import com.parentalcare.core.data.supabase.SupabasePaths
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch

@Singleton
class ScreenshotRequestRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val table = SupabasePaths.TABLE_SCREENSHOT_REQUESTS

    suspend fun createRequest(
        familyId: String,
        childDeviceId: String,
        delaySeconds: Int = 0,
        ttlMinutes: Long = 5,
    ): Result<ScreenshotRequestDoc> = resultOf {
        val uid = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("not authenticated")
        val requestId = "req_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val now = System.currentTimeMillis()
        
        val req = ScreenshotRequestDoc(
            requestId = requestId,
            familyId = familyId,
            parentUserId = uid,
            childDeviceId = childDeviceId,
            createdAt = now,
            expiresAt = now + ttlMinutes * 60 * 1000,
            nonce = com.parentalcare.core.security.pairing.freshNonce(),
            status = "REQUESTED",
            delaySeconds = delaySeconds,
        )

        supabase.postgrest[table].insert(req)
        Timber.tag(TAG).i("request created: reqId=%s dev=%s", Redactor.idPrefix(requestId), Redactor.idPrefix(childDeviceId))
        req
    }

    fun listenForDeviceRequests(familyId: String, deviceId: String): Flow<ScreenshotRequestDoc?> = callbackFlow {
        val channel = supabase.realtime.channel("requests:$familyId:$deviceId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            this.table = SupabasePaths.TABLE_SCREENSHOT_REQUESTS
            this.filter = "child_device_id=eq.$deviceId"
        }
        
        val job = launch {
            flow.collect { action ->
                try {
                    val req = supabase.postgrest[SupabasePaths.TABLE_SCREENSHOT_REQUESTS]
                        .select {
                            filter {
                                eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                                eq(SupabasePaths.Columns.CHILD_DEVICE_ID, deviceId)
                                eq(SupabasePaths.Columns.STATUS, "REQUESTED")
                            }
                        }
                        .decodeList<ScreenshotRequestDoc>()
                        .firstOrNull()
                    trySend(req)
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching device request")
                }
            }
        }
        
        supabase.realtime.connect()
        channel.subscribe()
        
        launch {
            try {
                val req = supabase.postgrest[SupabasePaths.TABLE_SCREENSHOT_REQUESTS]
                    .select {
                        filter {
                            eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                            eq(SupabasePaths.Columns.CHILD_DEVICE_ID, deviceId)
                            eq(SupabasePaths.Columns.STATUS, "REQUESTED")
                        }
                    }
                    .decodeList<ScreenshotRequestDoc>()
                    .firstOrNull()
                trySend(req)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching initial device request")
            }
        }

        awaitClose {
            job.cancel()
            launch { supabase.realtime.removeChannel(channel) }
        }
    }

    suspend fun updateStatus(
        request: ScreenshotRequestDoc,
        newStatus: String,
        failureReason: String? = null,
    ): Result<Unit> = resultOf {
        val updates = mutableMapOf<String, Any>(
            SupabasePaths.Columns.STATUS to newStatus,
        )
        if (newStatus in setOf("UPLOADED", "DELIVERED", "FAILED")) {
            updates[SupabasePaths.Columns.COMPLETED_AT] = System.currentTimeMillis()
        }
        if (failureReason != null) {
            updates[SupabasePaths.Columns.FAILURE_REASON] = failureReason
        }

        supabase.postgrest[SupabasePaths.TABLE_SCREENSHOT_REQUESTS].update(updates) {
            filter {
                eq(SupabasePaths.Columns.FAMILY_ID, request.familyId)
                eq(SupabasePaths.Columns.REQUEST_ID, request.requestId)
            }
        }
    }

    suspend fun getById(familyId: String, requestId: String): Result<ScreenshotRequestDoc?> = resultOf {
        supabase.postgrest[SupabasePaths.TABLE_SCREENSHOT_REQUESTS]
            .select {
                filter {
                    eq(SupabasePaths.Columns.FAMILY_ID, familyId)
                    eq(SupabasePaths.Columns.REQUEST_ID, requestId)
                }
            }
            .decodeSingleOrNull<ScreenshotRequestDoc>()
    }

    fun listenForParentRequests(familyId: String, limit: Long = 50L): Flow<List<ScreenshotRequestDoc>> = callbackFlow {
        val channel = supabase.realtime.channel("parent_requests:$familyId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            this.table = SupabasePaths.TABLE_SCREENSHOT_REQUESTS
            this.filter = "family_id=eq.$familyId"
        }
        
        val job = launch {
            flow.collect { action ->
                try {
                    val list = supabase.postgrest[SupabasePaths.TABLE_SCREENSHOT_REQUESTS]
                        .select { filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) } }
                        .decodeList<ScreenshotRequestDoc>()
                    trySend(list)
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching parent requests")
                }
            }
        }
        
        supabase.realtime.connect()
        channel.subscribe()
        
        launch {
            try {
                val list = supabase.postgrest[SupabasePaths.TABLE_SCREENSHOT_REQUESTS]
                    .select { filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) } }
                    .decodeList<ScreenshotRequestDoc>()
                trySend(list)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching initial parent requests")
            }
        }

        awaitClose {
            job.cancel()
            launch { supabase.realtime.removeChannel(channel) }
        }
    }

    private companion object { const val TAG = "PC.SupabaseRequestRepo" }
}
