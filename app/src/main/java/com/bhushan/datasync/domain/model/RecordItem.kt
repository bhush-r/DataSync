package com.bhushan.datasync.domain.model

/**
 * Generic user-created record used to demonstrate full CRUD
 * (Add / View / Update / Delete) against Firestore, stored at
 * `users/{uid}/records/{recordId}`.
 */
data class RecordItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
