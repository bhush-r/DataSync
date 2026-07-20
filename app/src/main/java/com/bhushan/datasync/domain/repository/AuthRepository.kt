package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** True if FirebaseAuth currently has a signed-in user (used for session restore). */
    fun isUserAuthenticated(): Boolean

    fun getCurrentUserId(): String?

    /**
     * Signs in with email/password, fetches (or lazily creates) the
     * corresponding `users/{uid}` Firestore profile, registers the FCM
     * token, and emits the resulting [User] on success.
     */
    fun login(email: String, password: String): Flow<Resource<User>>

    fun logout()
}
