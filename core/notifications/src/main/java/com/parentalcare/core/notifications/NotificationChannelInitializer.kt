package com.parentalcare.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.parentalcare.core.common.constants.AppConstants

/**
 * Centralized notification channel registration. Called from Application.onCreate().
 *
 * Channels:
 *   - channel_monitoring             (LOW)      — persistent foreground "Monitoring Active"
 *   - channel_screenshot_request     (HIGH)     — incoming screenshot request (child)
 *   - channel_screenshot_received    (DEFAULT)  — new screenshot received (parent)
 *   - channel_low                    (LOW)      — misc info
 */
object NotificationChannelInitializer {

    fun initialize(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService<NotificationManager>() ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                AppConstants.Channels.CHANNEL_MONITORING,
                "Monitoring Status",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Persistent indicator while Parental Care monitoring is active."
                setShowBadge(false)
            },
        )

        nm.createNotificationChannel(
            NotificationChannel(
                AppConstants.Channels.CHANNEL_SCREENSHOT_REQUEST,
                "Screenshot Requests",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifies child when parent requests a screenshot."
            },
        )

        nm.createNotificationChannel(
            NotificationChannel(
                AppConstants.Channels.CHANNEL_SCREENSHOT_RECEIVED,
                "Screenshots Received",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifies parent when a new screenshot is delivered."
            },
        )

        nm.createNotificationChannel(
            NotificationChannel(
                AppConstants.Channels.CHANNEL_LOW_PRIORITY,
                "General",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }
}
