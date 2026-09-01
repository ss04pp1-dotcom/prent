package com.parentalcare.child.fcm

import com.parentalcare.child.pipeline.IncomingRequestHandler
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.data.device.DeviceRepository
import com.parentalcare.core.notifications.BaseFcmService
import com.parentalcare.core.notifications.SafePayload
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ChildFcmService : BaseFcmService() {

    @Inject lateinit var requestHandler: IncomingRequestHandler
    @Inject lateinit var deviceRepository: DeviceRepository
    @Inject lateinit var childPreferences: ChildPreferences
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataMessage(payload: SafePayload) {
        when (payload.type) {
            "SCREENSHOT_REQUEST" -> {
                ioScope.launch {
                    requestHandler.handleIncomingRequest(payload)
                }
            }
            "REQUEST_CANCELLED" -> {
                requestHandler.handleCancelled(payload)
            }
            else -> {
                Timber.tag(TAG).w("unknown FCM type: %s", payload.type)
            }
        }
    }

    override fun onTokenRefresh(token: String) {
        Timber.tag(TAG).i("Child FCM token refreshed: %s...", token.take(8))
        ioScope.launch {
            val deviceId = childPreferences.deviceId.first() ?: return@launch
            val familyId = childPreferences.familyId.first() ?: return@launch
            deviceRepository.updateFcmToken(deviceId, familyId, token)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel("service destroyed")
    }
}
