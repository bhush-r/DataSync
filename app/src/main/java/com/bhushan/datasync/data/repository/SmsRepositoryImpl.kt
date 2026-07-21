package com.bhushan.datasync.data.repository

import android.content.Context
import android.provider.Telephony
import com.bhushan.datasync.domain.model.SmsItem
import com.bhushan.datasync.domain.repository.SmsRepository
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
class SmsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : SmsRepository {

    private fun smsCollection(uid: String) = firestore
        .collection(Constants.COLLECTION_USERS)
        .document(uid)
        .collection(Constants.SUBCOLLECTION_SMS)

    override suspend fun readDeviceSms(): Resource<List<SmsItem>> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<SmsItem>()
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val address = if (addressIdx >= 0) it.getString(addressIdx).orEmpty() else ""
                    val body = if (bodyIdx >= 0) it.getString(bodyIdx).orEmpty() else ""
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else 0L

                    // Deterministic ID = sender + timestamp + body hash -> duplicate-safe.
                    val id = "${address.filter { c -> c.isDigit() || c == '+' }}_${date}_${body.hashCode()}"
                    val preview = if (body.length > Constants.SMS_PREVIEW_MAX_LENGTH) {
                        body.take(Constants.SMS_PREVIEW_MAX_LENGTH) + "…"
                    } else body

                    messages.add(
                        SmsItem(
                            id = id,
                            sender = address.ifBlank { "Unknown" },
                            body = body,
                            preview = preview,
                            timestamp = date
                        )
                    )
                }
            }
            Resource.Success(messages)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to read SMS messages")
        }
    }

    override fun observeSyncedSms(uid: String): Flow<Resource<List<SmsItem>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = smsCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to load messages"))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SmsItem::class.java)?.copy(id = doc.id)
                }.orEmpty().sortedByDescending { it.timestamp }
                trySend(Resource.Success(items))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun syncSms(uid: String, messages: List<SmsItem>): Resource<Int> {
        return try {
            val collection = smsCollection(uid)
            val now = System.currentTimeMillis()
            messages.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { sms ->
                    val docRef = collection.document(sms.id)
                    batch.set(docRef, sms.copy(syncedAt = now))
                }
                batch.commit().await()
            }
            Resource.Success(messages.size)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sync SMS messages")
        }
    }

    override suspend fun getSyncedCount(uid: String): Int {
        return try {
            val snapshot = smsCollection(uid)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            snapshot.count.toInt()
        } catch (_: Exception) {
            0
        }
    }
}
