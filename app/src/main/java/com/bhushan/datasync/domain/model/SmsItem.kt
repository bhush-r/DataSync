package com.bhushan.datasync.domain.model

/**
 * A single device SMS message. [id] is derived from (sender + timestamp +
 * a hash of the body) to keep sync idempotent and duplicate-free.
 */
data class SmsItem(
    val id: String = "",
    val sender: String = "",
    val body: String = "",
    val preview: String = "",
    val timestamp: Long = 0L,
    val syncedAt: Long = 0L
)
