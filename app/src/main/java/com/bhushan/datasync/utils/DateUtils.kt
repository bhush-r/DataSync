package com.bhushan.datasync.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val displayFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val shortFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    fun formatTimestamp(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "-"
        return displayFormat.format(Date(timestampMillis))
    }

    fun formatShort(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "-"
        return shortFormat.format(Date(timestampMillis))
    }

    /** Formats call duration (seconds) as mm:ss */
    fun formatDuration(seconds: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    fun relativeTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "Never synced"
        val diff = System.currentTimeMillis() - timestampMillis
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hr ago"
            else -> formatTimestamp(timestampMillis)
        }
    }
}
