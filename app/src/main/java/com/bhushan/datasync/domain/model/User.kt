package com.bhushan.datasync.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role = Role.USER,
    val fcmToken: String? = null,
    val devModeEnabled: Boolean = false,
    val lastSyncAt: Long = 0L,
    val lastLogin: Long = 0L,
    val createdAt: Long = 0L
)