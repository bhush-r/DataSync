package com.bhushan.datasync.domain.model

/**
 * Fine-grained state of a sync run, observed by the Dashboard and each list
 * screen to render a progress bar / success toast / retryable error banner.
 */
sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(val label: String, val progressPercent: Int) : SyncState()
    data class Success(val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Aggregated counters shown on the Dashboard screen.
 */
data class DashboardStats(
    val totalContacts: Int = 0,
    val totalCallLogs: Int = 0,
    val totalSms: Int = 0,
    val lastSyncAt: Long = 0L,
    val role: Role = Role.USER,
    val devModeEnabled: Boolean = false
)
