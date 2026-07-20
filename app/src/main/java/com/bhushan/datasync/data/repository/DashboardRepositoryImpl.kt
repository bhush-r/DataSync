package com.bhushan.datasync.data.repository

import com.bhushan.datasync.domain.model.DashboardStats
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.ContactRepository
import com.bhushan.datasync.domain.repository.CallLogRepository
import com.bhushan.datasync.domain.repository.DashboardRepository
import com.bhushan.datasync.domain.repository.SmsRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val smsRepository: SmsRepository
) : DashboardRepository {

    override suspend fun getDashboardStats(uid: String): Resource<DashboardStats> = coroutineScope {
        try {
            val userDeferred = async {
                firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                    .toObject(User::class.java)
            }
            val contactsDeferred = async { contactRepository.getSyncedCount(uid) }
            val callLogsDeferred = async { callLogRepository.getSyncedCount(uid) }
            val smsDeferred = async { smsRepository.getSyncedCount(uid) }

            val results = awaitAll(userDeferred, contactsDeferred, callLogsDeferred, smsDeferred)
            val user = results[0] as? User

            Resource.Success(
                DashboardStats(
                    totalContacts = results[1] as Int,
                    totalCallLogs = results[2] as Int,
                    totalSms = results[3] as Int,
                    lastSyncAt = user?.lastSyncAt ?: 0L,
                    role = user?.role ?: com.bhushan.datasync.domain.model.Role.USER,
                    devModeEnabled = user?.devModeEnabled ?: false
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load dashboard")
        }
    }
}
