package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.ContactItem
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    /** Reads contacts straight from the device ContentResolver. Requires READ_CONTACTS. */
    suspend fun readDeviceContacts(): Resource<List<ContactItem>>

    /** Streams what has already been synced to Firestore for the given user. */
    fun observeSyncedContacts(uid: String): Flow<Resource<List<ContactItem>>>

    /**
     * Uploads [contacts] to Firestore using deterministic document IDs so
     * running sync multiple times never creates duplicates. Returns the
     * count of newly written/updated documents.
     */
    suspend fun syncContacts(uid: String, contacts: List<ContactItem>): Resource<Int>

    suspend fun getSyncedCount(uid: String): Int
}
