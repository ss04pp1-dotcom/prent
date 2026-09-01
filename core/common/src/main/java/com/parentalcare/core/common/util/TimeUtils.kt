package com.parentalcare.core.common.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pure time helpers. No Android dependencies so this is unit-testable.
 */
object TimeUtils {

    private val deviceZone: TimeZone = TimeZone.getDefault()

    /** "5:42 PM" — used in screenshot lists. */
    fun formatTime(epochMillis: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.timeZone = deviceZone
        return sdf.format(Date(epochMillis))
    }

    /** "Today, 5:42 PM" — used in cards. */
    fun formatDateTime(epochMillis: Long): String {
        val today = todayMidnight()
        val yesterday = todayMidnight() - DAY_MS
        val target = epochMillis.startOfDay()
        val date = when (target) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(Date(epochMillis))
        }
        return "$date, ${formatTime(epochMillis)}"
    }

    /** "Aug 25, 2024" — calendar history view. */
    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.timeZone = deviceZone
        return sdf.format(Date(epochMillis))
    }

    /** Returns true if both timestamps fall on the same calendar day. */
    fun isSameDay(a: Long, b: Long): Boolean =
        a.startOfDay() == b.startOfDay()

    fun isToday(epochMillis: Long): Boolean =
        isSameDay(epochMillis, System.currentTimeMillis())

    fun isYesterday(epochMillis: Long): Boolean =
        isSameDay(epochMillis, System.currentTimeMillis() - DAY_MS)

    fun todayMidnight(): Long = System.currentTimeMillis().startOfDay()

    private const val DAY_MS = 24L * 60 * 60 * 1000

    private fun Long.startOfDay(): Long {
        val cal = Calendar.getInstance(deviceZone).apply {
            timeInMillis = this@startOfDay
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /** ISO-8601 for Firestore storage consistency. */
    fun toIso(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        sdf.timeZone = deviceZone
        return sdf.format(Date(epochMillis))
    }
}
