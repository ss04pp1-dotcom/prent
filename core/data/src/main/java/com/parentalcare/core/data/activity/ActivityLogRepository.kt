package com.parentalcare.core.data.activity

import com.parentalcare.core.common.model.ActivityEvent
import com.parentalcare.core.common.result.Result
import com.parentalcare.core.common.result.resultOf
import com.parentalcare.core.data.supabase.SupabasePaths
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch

@Singleton
class ActivityLogRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val table = SupabasePaths.TABLE_ACTIVITY_LOG

    suspend fun log(event: ActivityEvent): Result<Unit> = resultOf {
        val uid = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("not authenticated")
        val eventWithId = event.copy(
            eventId = "evt_${event.timestamp}_${System.nanoTime().toString(16)}"
        )
        
        supabase.postgrest[table].insert(eventWithId)
        Timber.tag(TAG).i("activity logged: type=%s actor=%s", event.type.name, event.actorType.name)
    }

    fun listenForActivityLog(familyId: String, limit: Long = 100L): Flow<List<ActivityEvent>> = callbackFlow {
        val channel = supabase.realtime.channel("activity_log:$familyId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            this.table = SupabasePaths.TABLE_ACTIVITY_LOG
            this.filter = "family_id=eq.$familyId"
        }
        
        val job = launch {
            flow.collect { action ->
                try {
                    val events = supabase.postgrest[SupabasePaths.TABLE_ACTIVITY_LOG]
                        .select {
                            filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) }
                            order(SupabasePaths.Columns.CREATED_AT, Order.DESCENDING)
                            limit(limit)
                        }
                        .decodeList<ActivityEvent>()
                    trySend(events)
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching activity logs")
                }
            }
        }
        
        supabase.realtime.connect()
        channel.subscribe()
        
        launch {
            try {
                val events = supabase.postgrest[SupabasePaths.TABLE_ACTIVITY_LOG]
                    .select {
                        filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) }
                        order(SupabasePaths.Columns.CREATED_AT, Order.DESCENDING)
                        limit(limit)
                    }
                    .decodeList<ActivityEvent>()
                trySend(events)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching initial activity logs")
            }
        }

        awaitClose {
            job.cancel()
            launch { supabase.realtime.removeChannel(channel) }
        }
    }

    suspend fun getRecentActivity(familyId: String, limit: Long = 50L): Result<List<ActivityEvent>> = resultOf {
        supabase.postgrest[table]
            .select {
                filter { eq(SupabasePaths.Columns.FAMILY_ID, familyId) }
                order(SupabasePaths.Columns.CREATED_AT, Order.DESCENDING)
                limit(limit)
            }
            .decodeList<ActivityEvent>()
    }

    private companion object { const val TAG = "PC.SupabaseActivityLogRepo" }
}
