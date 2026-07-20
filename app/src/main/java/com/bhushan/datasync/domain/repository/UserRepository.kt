package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    /** Realtime listener on `users/{uid}` -- drives role/devMode changes across the UI instantly. */
    fun observeCurrentUser(uid: String): Flow<Resource<User>>

    suspend fun updateFcmToken(uid: String, token: String)

    suspend fun updateLastSyncTimestamp(uid: String, timestamp: Long)

    /** Admin-only: toggles the Development Mode flag for their own profile. */
    suspend fun setDevMode(uid: String, enabled: Boolean): Resource<Unit>
}
