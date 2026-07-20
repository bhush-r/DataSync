package com.bhushan.datasync.domain.model

/**
 * Represents the `users/{uid}` Firestore document. A no-arg constructor and
 * mutable `var` fields with default values are required so Firestore's
 * reflection-based deserializer (`toObject<User>()`) can build this class,
 * while still giving us a clean immutable-looking domain object everywhere
 * else in the app.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val role: Role = Role.USER,
    val fcmToken: String? = null,
    val devModeEnabled: Boolean = false,
    val lastSyncAt: Long = 0L,
    val createdAt: Long = 0L
)
