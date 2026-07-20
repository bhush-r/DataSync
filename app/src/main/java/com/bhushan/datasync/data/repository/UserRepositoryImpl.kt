package com.bhushan.datasync.data.repository

import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.UserRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) : UserRepository {

    override fun observeCurrentUser(uid: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = firestore.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to load profile"))
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("User profile not found"))
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllUsers(): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = firestore.collection(Constants.COLLECTION_USERS)
            .orderBy(Constants.FIELD_CREATED_AT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to load users"))
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                }.orEmpty()
                trySend(Resource.Success(users))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun updateFcmToken(uid: String, token: String) {
        try {
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(Constants.FIELD_FCM_TOKEN, token).await()
        } catch (_: Exception) {}
    }

    override suspend fun updateLastSyncTimestamp(uid: String, timestamp: Long) {
        try {
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(Constants.FIELD_LAST_SYNC_AT, timestamp).await()
            sessionManager.updateLastSync(timestamp)
        } catch (_: Exception) {}
    }

    override suspend fun setDevMode(uid: String, enabled: Boolean): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(Constants.FIELD_DEV_MODE_ENABLED, enabled).await()
            sessionManager.updateDevMode(enabled)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update development mode")
        }
    }
}