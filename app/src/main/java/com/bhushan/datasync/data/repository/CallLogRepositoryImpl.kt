package com.bhushan.datasync.data.repository

import android.content.Context
import android.provider.CallLog
import com.bhushan.datasync.domain.model.CallLogItem
import com.bhushan.datasync.domain.model.CallType
import com.bhushan.datasync.domain.repository.CallLogRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : CallLogRepository {

    private fun callLogsCollection(uid: String) = firestore
        .collection(Constants.COLLECTION_USERS)
        .document(uid)
        .collection(Constants.SUBCOLLECTION_CALL_LOGS)

    override suspend fun readDeviceCallLogs(): Resource<List<CallLogItem>> = withContext(Dispatchers.IO) {
        try {
            val logs = mutableListOf<CallLogItem>()
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE
                ),
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)

                while (it.moveToNext()) {
                    val number = if (numberIdx >= 0) it.getString(numberIdx).orEmpty() else ""
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else 0L
                    val callType = mapCallType(if (typeIdx >= 0) it.getInt(typeIdx) else -1)
                    val duration = if (durationIdx >= 0) it.getLong(durationIdx) else 0L
                    val name = if (nameIdx >= 0) it.getString(nameIdx).orEmpty() else ""

                    // Deterministic ID = number + timestamp -> prevents duplicates on re-sync.
                    val id = "${number.filter { c -> c.isDigit() }}_$date"

                    logs.add(
                        CallLogItem(
                            id = id,
                            name = name.ifBlank { "Unknown" },
                            phoneNumber = number,
                            callType = callType,
                            durationSeconds = duration,
                            timestamp = date
                        )
                    )
                }
            }
            Resource.Success(logs)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to read call logs")
        }
    }

    private fun mapCallType(type: Int): CallType = when (type) {
        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
        else -> CallType.UNKNOWN
    }

    override fun observeSyncedCallLogs(uid: String): Flow<Resource<List<CallLogItem>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = callLogsCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to load call logs"))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CallLogItem::class.java)?.copy(id = doc.id)
                }.orEmpty().sortedByDescending { it.timestamp }
                trySend(Resource.Success(items))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun syncCallLogs(uid: String, callLogs: List<CallLogItem>): Resource<Int> {
        return try {
            val collection = callLogsCollection(uid)
            val now = System.currentTimeMillis()
            callLogs.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { log ->
                    val docRef = collection.document(log.id)
                    batch.set(docRef, log.copy(syncedAt = now))
                }
                batch.commit().await()
            }
            Resource.Success(callLogs.size)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sync call logs")
        }
    }

    override suspend fun getSyncedCount(uid: String): Int {
        return try {
            val snapshot = callLogsCollection(uid)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            snapshot.count.toInt()
        } catch (_: Exception) {
            0
        }
    }
}
