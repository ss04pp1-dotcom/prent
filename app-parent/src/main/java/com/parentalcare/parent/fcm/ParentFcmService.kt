package com.parentalcare.parent.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.parentalcare.parent.MainActivity
import com.parentalcare.core.common.constants.AppConstants
import com.parentalcare.core.notifications.BaseFcmService
import com.parentalcare.core.notifications.SafePayload
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Parent-side FCM service. Listens for:
 *   - "SCREENSHOT_RECEIVED" — a new screenshot is ready in the inbox.
 *     Notification text intentionally says only "New screenshot received."
 *     — NEVER includes screenshot bytes / download URL / preview image.
 *
 * Notifications also respect the user's per-channel toggle settings
 * (see [com.parentalcare.parent.ui.screen.NotificationsSettingsScreen]).
 */
@AndroidEntryPoint
class ParentFcmService : BaseFcmService() {

    override fun onDataMessage(payload: SafePayload) {
        when (payload.type) {
            "SCREENSHOT_RECEIVED" -> {
                Timber.tag(TAG).i("showing screenshot received notification")
                showReceivedNotification(payload)
            }
            "DEVICE_ONLINE", "DEVICE_OFFLINE" -> {
                Timber.tag(TAG).i("device status update: %s", payload.type)
                showDeviceStatusNotification(payload)
            }
            else -> {
                Timber.tag(TAG).w("unknown FCM type: %s", payload.type)
            }
        }
    }

    override fun onTokenRefresh(token: String) {
        // Parent app doesn't store its own FCM token in Firestore — the
        // parent's device is identified by Firebase Authentication UID.
        // FCM token refresh is handled by Firebase SDK.
    }

    private fun showReceivedNotification(payload: SafePayload) {
        val mgr = getSystemService<NotificationManager>() ?: return
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_type", "SCREENSHOT_RECEIVED")
                putExtra("screenshotId", payload.screenshotId)
                putExtra("familyId", payload.familyId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notif = NotificationCompat.Builder(this, CHANNEL_RECEIVED)
            .setContentTitle("Parental Care")
            .setContentText("New screenshot received.")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()
        mgr.notify(AppConstants.Notifications.SCREENSHOT_RECEIVED, notif)
    }

    private fun showDeviceStatusNotification(payload: SafePayload) {
        val mgr = getSystemService<NotificationManager>() ?: return
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_type", payload.type)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val msg = if (payload.type == "DEVICE_ONLINE") "Child device is now online." else "Child device went offline."
        val notif = NotificationCompat.Builder(this, CHANNEL_LOW)
            .setContentTitle("Parental Care")
            .setContentText(msg)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()
        mgr.notify(AppConstants.Notifications.DEVICE_STATUS, notif)
    }

    private companion object {
        const val CHANNEL_RECEIVED = "channel_screenshot_received"
        const val CHANNEL_LOW = "channel_low"
        const val TAG = "PC.ParentFcm"
    }
}
