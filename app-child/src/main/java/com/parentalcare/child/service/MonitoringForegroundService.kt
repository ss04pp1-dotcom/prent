package com.parentalcare.child.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.parentalcare.child.MainActivity
import com.parentalcare.child.R
import com.parentalcare.child.pipeline.IncomingRequestHandler
import com.parentalcare.core.common.constants.AppConstants
import com.parentalcare.core.data.prefs.ChildPreferences
import com.parentalcare.core.data.request.ScreenshotRequestRepository
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
class MonitoringForegroundService : Service() {

    @Inject lateinit var childPreferences: ChildPreferences
    @Inject lateinit var requestRepository: ScreenshotRequestRepository
    @Inject lateinit var incomingRequestHandler: IncomingRequestHandler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notif = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                AppConstants.Services.MONITORING_SERVICE_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(AppConstants.Services.MONITORING_SERVICE_ID, notif)
        }

        startSupabaseListener()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, AppConstants.Channels.CHANNEL_MONITORING)
            .setContentTitle(getString(R.string.monitoring_notification_title))
            .setContentText(getString(R.string.monitoring_notification_text))
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startSupabaseListener() {
        serviceScope.launch {
            val deviceId = childPreferences.deviceId.first() ?: return@launch
            val familyId = childPreferences.familyId.first() ?: return@launch

            requestRepository.listenForDeviceRequests(familyId, deviceId).collect { req ->
                if (req != null && req.status == "REQUESTED") {
                    Timber.tag("PC.MonitorSvc").i("Received request via Supabase: ${req.requestId}")
                    val payload = SafePayload(
                        type = "SCREENSHOT_REQUEST",
                        requestId = req.requestId,
                        familyId = req.familyId
                    )
                    incomingRequestHandler.handleIncomingRequest(payload)
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MonitoringForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitoringForegroundService::class.java))
        }
    }
}
