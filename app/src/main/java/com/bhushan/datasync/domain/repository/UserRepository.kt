package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrentUser(uid: String): Flow<Resource<User>>
    fun getAllUsers(): Flow<Resource<List<User>>>
    suspend fun updateFcmToken(uid: String, token: String)
    suspend fun updateLastSyncTimestamp(uid: String, timestamp: Long)
    suspend fun setDevMode(uid: String, enabled: Boolean): Resource<Unit>
}