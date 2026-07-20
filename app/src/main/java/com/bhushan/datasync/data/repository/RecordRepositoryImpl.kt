package com.bhushan.datasync.data.repository

import com.bhushan.datasync.domain.model.RecordItem
import com.bhushan.datasync.domain.repository.RecordRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RecordRepository {

    private fun recordsCollection(uid: String) = firestore
        .collection(Constants.COLLECTION_USERS)
        .document(uid)
        .collection(Constants.SUBCOLLECTION_RECORDS)

    override fun observeRecords(uid: String): Flow<Resource<List<RecordItem>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = recordsCollection(uid)
            .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to load records"))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RecordItem::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(Resource.Success(items))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addRecord(uid: String, title: String, description: String): Resource<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val docRef = recordsCollection(uid).document()
            val record = RecordItem(
                id = docRef.id,
                title = title,
                description = description,
                createdAt = now,
                updatedAt = now
            )
            docRef.set(record).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add record")
        }
    }

    override suspend fun updateRecord(uid: String, record: RecordItem): Resource<Unit> {
        return try {
            val updated = record.copy(updatedAt = System.currentTimeMillis())
            recordsCollection(uid).document(record.id).set(updated).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update record")
        }
    }

    override suspend fun deleteRecord(uid: String, recordId: String): Resource<Unit> {
        return try {
            recordsCollection(uid).document(recordId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete record")
        }
    }
}
