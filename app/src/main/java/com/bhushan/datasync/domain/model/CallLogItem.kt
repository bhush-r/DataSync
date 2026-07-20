package com.bhushan.datasync.domain.model

enum class CallType {
    INCOMING, OUTGOING, MISSED, UNKNOWN
}

/**
 * A single device call-log entry. [id] is deterministically derived from
 * (number + timestamp) so re-reading the same call log twice never creates
 * a duplicate Firestore document.
 */
data class CallLogItem(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val callType: CallType = CallType.UNKNOWN,
    val durationSeconds: Long = 0L,
    val timestamp: Long = 0L,
    val syncedAt: Long = 0L
)
