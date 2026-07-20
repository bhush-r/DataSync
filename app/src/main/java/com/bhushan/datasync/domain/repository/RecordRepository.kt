package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.RecordItem
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Full CRUD contract for the generic "Records" feature (requirement #16).
 * Backed by Firestore collection `users/{uid}/records`.
 */
interface RecordRepository {

    fun observeRecords(uid: String): Flow<Resource<List<RecordItem>>>

    suspend fun addRecord(uid: String, title: String, description: String): Resource<Unit>

    suspend fun updateRecord(uid: String, record: RecordItem): Resource<Unit>

    suspend fun deleteRecord(uid: String, recordId: String): Resource<Unit>
}
