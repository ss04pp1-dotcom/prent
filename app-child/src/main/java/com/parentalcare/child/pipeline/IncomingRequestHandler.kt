package com.parentalcare.child.pipeline

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.parentalcare.core.common.constants.AppConstants
import com.parentalcare.core.data.model.ScreenshotRequestDoc
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.data.request.ScreenshotRequestRepository
import com.parentalcare.core.notifications.SafePayload
import com.parentalcare.core.security.pairing.NonceRegistry
import com.parentalcare.core.security.pairing.ScreenshotRequest
import com.parentalcare.core.security.pairing.ScreenshotRequestValidator
import com.parentalcare.core.common.result.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomingRequestHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val requestRepo: ScreenshotRequestRepository,
    private val nonceRegistry: NonceRegistry,
    private val childPreferences: ChildPreferences,
) {
    private val _active = MutableStateFlow<ScreenshotRequestDoc?>(null)
    val active: StateFlow<ScreenshotRequestDoc?> = _active.asStateFlow()

    private val timeoutScope = CoroutineScope(Dispatchers.IO)
    private var timeoutJob: Job? = null

    suspend fun handleIncomingRequest(payload: SafePayload) {
        val requestId = payload.requestId ?: return
        if (!nonceRegistry.isFresh(requestId)) return
        val familyId = payload.familyId ?: return
        
        val docResult = requestRepo.getById(familyId, requestId)
        val req = docResult.getOrNull() ?: return

        // Validate request using client-side validator
        val deviceId = childPreferences.deviceId.first() ?: return
        val screenshotRequest = ScreenshotRequest(
            requestId = req.requestId,
            familyId = req.familyId,
            parentUserId = req.parentUserId,
            childDeviceId = req.childDeviceId,
            createdAt = req.createdAt ?: System.currentTimeMillis(),
            expiresAt = req.expiresAt,
            nonce = req.nonce,
            status = req.status,
        )
        
        if (!ScreenshotRequestValidator.isValid(screenshotRequest, familyId, deviceId)) {
            Timber.tag("PC.IncomingHandler").w("Request validation failed: reqId=$requestId")
            requestRepo.updateStatus(req, "FAILED", "Request validation failed")
            return
        }

        // 1. Mark as processing
        requestRepo.updateStatus(req, "PROCESSING")

        // 2. Notify UI to prompt user for capture (NO automatic silent capture)
        _active.value = req
        showRequestNotification(payload)

        // 3. Start timeout watcher
        startTimeoutWatcher(req)
    }

    fun handleCancelled(payload: SafePayload) {
        _active.value = null
        cancelTimeoutWatcher()
    }

    private fun startTimeoutWatcher(req: ScreenshotRequestDoc) {
        cancelTimeoutWatcher()
        val now = System.currentTimeMillis()
        val timeoutDelay = req.expiresAt - now
        if (timeoutDelay > 0) {
            timeoutJob = timeoutScope.launch {
                delay(timeoutDelay)
                onRequestTimeout(req)
            }
        }
    }

    private fun cancelTimeoutWatcher() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    private fun onRequestTimeout(req: ScreenshotRequestDoc) {
        Timber.tag("PC.IncomingHandler").i("Request timed out: ${req.requestId}")
        
        // Update status to EXPIRED
        timeoutScope.launch { requestRepo.updateStatus(req, "EXPIRED", "Request timed out") }
        
        // Clear active request
        _active.value = null
        
        // Show timeout notification to child
        showTimeoutNotification(req)
    }

    private fun showRequestNotification(payload: SafePayload) {
        val mgr = context.getSystemService<NotificationManager>() ?: return
        val notif = NotificationCompat.Builder(context, AppConstants.Channels.CHANNEL_SCREENSHOT_REQUEST)
            .setContentTitle("Screenshot Requested")
            .setContentText("Your parent requested a screenshot. Tap to allow.")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        mgr.notify(AppConstants.Notifications.SCREENSHOT_REQUEST, notif)
    }

    private fun showTimeoutNotification(req: ScreenshotRequestDoc) {
        val mgr = context.getSystemService<NotificationManager>() ?: return
        val notif = NotificationCompat.Builder(context, AppConstants.Channels.CHANNEL_SCREENSHOT_REQUEST)
            .setContentTitle("Screenshot Request Expired")
            .setContentText("The screenshot request from your parent has timed out.")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        mgr.notify(AppConstants.Notifications.REQUEST_TIMEOUT, notif)
    }
}
