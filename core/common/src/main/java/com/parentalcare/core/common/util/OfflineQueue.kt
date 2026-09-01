package com.parentalcare.core.common.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.flow.first

/**
 * Offline queue for Supabase operations.
 * Stores pending operations locally and replays them when network is available.
 */
@Serializable
data class OfflineOperation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: OperationType,
    val payload: String, // JSON serialized request
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
) {
    enum class OperationType {
        UPLOAD_SCREENSHOT,
        UPDATE_REQUEST_STATUS,
        UPDATE_DEVICE_STATUS,
        INCREMENT_COUNTERS,
        MARK_VIEWED,
        DELETE_SCREENSHOT,
        SYNC_ACTIVITY_LOG
    }
}

class OfflineQueueManager private constructor(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val queue: Channel<OfflineOperation>,
    private val _isProcessing: MutableStateFlow<Boolean>,
    private val _pendingCount: MutableStateFlow<Int>
) {
    val isProcessing: StateFlow<Boolean> = _isProcessing
    val pendingCount: StateFlow<Int> = _pendingCount

    private val json = Json { ignoreUnknownKeys = true }
    private val OPERATIONS_KEY = stringPreferencesKey("offline_operations")
    private val MAX_RETRIES = 5
    private val MAX_QUEUE_SIZE = 1000

    companion object {
        @Volatile private var INSTANCE: OfflineQueueManager? = null
        
        fun getInstance(context: Context): OfflineQueueManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineQueueManager(context).also { INSTANCE = it }
            }
        }

        private fun OfflineQueueManager(context: Context): OfflineQueueManager {
            val dataStore = context.dataStore
            val queue = Channel<OfflineOperation>(1000)
            val isProcessing = MutableStateFlow(false)
            val pendingCount = MutableStateFlow(0)
            
            val instance = OfflineQueueManager(context, dataStore, queue, isProcessing, pendingCount)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { instance.loadPendingCount() }
            instance.startProcessor()
            return instance
        }
    }

    suspend fun enqueue(operation: OfflineOperation): Boolean {
        if (_pendingCount.value >= MAX_QUEUE_SIZE) {
            return false
        }
        
        val operations = getStoredOperations()
        if (operations.size >= MAX_QUEUE_SIZE) {
            return false
        }
        
        operations.add(operation)
        saveOperations(operations)
        _pendingCount.value = operations.size
        
        // Try to send immediately if not processing
        if (!_isProcessing.value) {
            queue.trySend(operation)
        }
        return true
    }

    private suspend fun loadPendingCount() {
        val operations = getStoredOperations()
        _pendingCount.value = operations.size
    }

    private suspend fun getStoredOperations(): MutableList<OfflineOperation> {
        val jsonString = dataStore.data.map { it[OPERATIONS_KEY] ?: "" }.first()
        return if (jsonString.isNotEmpty()) {
            json.decodeFromString<List<OfflineOperation>>(jsonString).toMutableList()
        } else {
            mutableListOf()
        }
    }

    private suspend fun saveOperations(operations: List<OfflineOperation>) {
        val jsonString = json.encodeToString(operations)
        dataStore.edit { it[OPERATIONS_KEY] = jsonString }
    }

    internal fun startProcessor() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            for (operation in queue) {
                _isProcessing.value = true
                processOperation(operation)
                _isProcessing.value = false
            }
        }
    }

    private suspend fun processOperation(operation: OfflineOperation) {
        val result = when (operation.type) {
            OfflineOperation.OperationType.UPLOAD_SCREENSHOT -> processScreenshotUpload(operation)
            OfflineOperation.OperationType.UPDATE_REQUEST_STATUS -> processRequestStatusUpdate(operation)
            OfflineOperation.OperationType.UPDATE_DEVICE_STATUS -> processDeviceStatusUpdate(operation)
            OfflineOperation.OperationType.INCREMENT_COUNTERS -> processCounterIncrement(operation)
            OfflineOperation.OperationType.MARK_VIEWED -> processMarkViewed(operation)
            OfflineOperation.OperationType.DELETE_SCREENSHOT -> processDeleteScreenshot(operation)
            OfflineOperation.OperationType.SYNC_ACTIVITY_LOG -> processActivityLogSync(operation)
        }

        if (result.isFailure && operation.retryCount < MAX_RETRIES) {
            // Re-queue with incremented retry count
            val retryOp = operation.copy(retryCount = operation.retryCount + 1)
            enqueue(retryOp)
        } else if (result.isSuccess) {
            // Remove from persistent storage
            removeOperation(operation.id)
        }
    }

    private suspend fun removeOperation(id: String) {
        val operations = getStoredOperations()
        operations.removeAll { it.id == id }
        saveOperations(operations)
        _pendingCount.value = operations.size
    }

    // Processors - these should be implemented with actual Supabase calls
    private suspend fun processScreenshotUpload(op: OfflineOperation): Result<Unit> = Result.success(Unit)
    private suspend fun processRequestStatusUpdate(op: OfflineOperation): Result<Unit> = Result.success(Unit)
    private suspend fun processDeviceStatusUpdate(op: OfflineOperation): Result<Unit> = Result.success(Unit)
    private suspend fun processCounterIncrement(op: OfflineOperation): Result<Unit> = Result.success(Unit)
    private suspend fun processMarkViewed(op: OfflineOperation): Result<Unit> = Result.success(Unit)
    private suspend fun processDeleteScreenshot(op: OfflineOperation): Result<Unit> = Result.success(Unit)
    private suspend fun processActivityLogSync(op: OfflineOperation): Result<Unit> = Result.success(Unit)

    // Public API for specific operations
    suspend fun queueScreenshotUpload(requestJson: String) = enqueue(
        OfflineOperation(type = OfflineOperation.OperationType.UPLOAD_SCREENSHOT, payload = requestJson)
    )

    suspend fun queueRequestStatusUpdate(requestId: String, status: String, failureReason: String?) = enqueue(
        OfflineOperation(
            type = OfflineOperation.OperationType.UPDATE_REQUEST_STATUS,
            payload = org.json.JSONObject(mapOf("requestId" to requestId, "status" to status, "failureReason" to failureReason)).toString()
        )
    )

    suspend fun queueDeviceStatusUpdate(deviceId: String, familyId: String, isOnline: Boolean) = enqueue(
        OfflineOperation(
            type = OfflineOperation.OperationType.UPDATE_DEVICE_STATUS,
            payload = org.json.JSONObject(mapOf("deviceId" to deviceId, "familyId" to familyId, "isOnline" to isOnline)).toString()
        )
    )

    suspend fun queueCounterIncrement(deviceId: String, familyId: String, screenshot: Boolean, request: Boolean) = enqueue(
        OfflineOperation(
            type = OfflineOperation.OperationType.INCREMENT_COUNTERS,
            payload = org.json.JSONObject(mapOf("deviceId" to deviceId, "familyId" to familyId, "screenshot" to screenshot, "request" to request)).toString()
        )
    )

    suspend fun queueMarkViewed(familyId: String, screenshotId: String) = enqueue(
        OfflineOperation(
            type = OfflineOperation.OperationType.MARK_VIEWED,
            payload = org.json.JSONObject(mapOf("familyId" to familyId, "screenshotId" to screenshotId)).toString()
        )
    )

    suspend fun queueDeleteScreenshot(familyId: String, screenshotId: String) = enqueue(
        OfflineOperation(
            type = OfflineOperation.OperationType.DELETE_SCREENSHOT,
            payload = org.json.JSONObject(mapOf("familyId" to familyId, "screenshotId" to screenshotId)).toString()
        )
    )

    suspend fun queueActivityLog(eventType: String, details: Map<String, Any>) = enqueue(
        OfflineOperation(
            type = OfflineOperation.OperationType.SYNC_ACTIVITY_LOG,
            payload = org.json.JSONObject(mapOf("eventType" to eventType, "details" to org.json.JSONObject(details))).toString()
        )
    )
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_queue")