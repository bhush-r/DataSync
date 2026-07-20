package com.bhushan.datasync.domain.model

import com.bhushan.datasync.utils.Constants

/**
 * The two roles supported by the machine test. Stored as a plain string in
 * Firestore ("ADMIN" / "USER") so security rules can pattern match on it
 * without needing a custom claims setup (see firestore.rules).
 */
enum class Role {
    ADMIN,
    USER;

    val isAdmin: Boolean get() = this == ADMIN

    companion object {
        fun fromString(value: String?): Role = when (value?.uppercase()) {
            Constants.ROLE_ADMIN -> ADMIN
            else -> USER
        }
    }
}
