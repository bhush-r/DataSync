package com.bhushan.datasync.domain.model

/**
 * A single device contact. [id] is a deterministic, normalized key
 * (usually the normalized phone number) used both as the RecyclerView
 * diff key and as the Firestore document ID -- this is what guarantees
 * duplicate prevention when the same contact is synced more than once.
 */
data class ContactItem(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val syncedAt: Long = 0L
)
