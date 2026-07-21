package com.bhushan.datasync.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.bhushan.datasync.domain.model.ContactItem
import com.bhushan.datasync.domain.repository.ContactRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.normalizePhoneNumber
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
import com.google.firebase.firestore.AggregateSource

@Singleton
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : ContactRepository {

    private fun contactsCollection(uid: String) = firestore
        .collection(Constants.COLLECTION_USERS)
        .document(uid)
        .collection(Constants.SUBCOLLECTION_CONTACTS)

    override suspend fun readDeviceContacts(): Resource<List<ContactItem>> = withContext(Dispatchers.IO) {
        try {
            val contacts = mutableListOf<ContactItem>()
            val seenIds = HashSet<String>()

            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = if (nameIdx >= 0) it.getString(nameIdx).orEmpty() else ""
                    val number = if (numberIdx >= 0) it.getString(numberIdx).orEmpty() else ""
                    if (number.isBlank()) continue

                    val normalizedId = number.normalizePhoneNumber()
                    if (normalizedId.isBlank() || !seenIds.add(normalizedId)) continue // skip on-device duplicates too

                    contacts.add(
                        ContactItem(
                            id = normalizedId,
                            name = name.ifBlank { "Unknown" },
                            phoneNumber = number
                        )
                    )
                }
            }
            Resource.Success(contacts)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to read contacts")
        }
    }

    override fun observeSyncedContacts(uid: String): Flow<Resource<List<ContactItem>>> = callbackFlow {
        trySend(Resource.Loading)
        val registration = contactsCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to load contacts"))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ContactItem::class.java)?.copy(id = doc.id)
                }.orEmpty().sortedBy { it.name.lowercase() }
                trySend(Resource.Success(items))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun syncContacts(uid: String, contacts: List<ContactItem>): Resource<Int> {
        return try {
            val collection = contactsCollection(uid)
            val now = System.currentTimeMillis()
            // Firestore limits a single batch to 500 writes -- chunk defensively
            // so large contact lists still sync correctly in one call.
            contacts.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { contact ->
                    // Deterministic document ID (normalized phone number) is the
                    // duplicate-prevention mechanism: re-syncing simply overwrites
                    // the same document instead of creating a new one.
                    val docRef = collection.document(contact.id)
                    batch.set(docRef, contact.copy(syncedAt = now))
                }
                batch.commit().await()
            }
            Resource.Success(contacts.size)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sync contacts")
        }
    }

    override suspend fun getSyncedCount(uid: String): Int {
        return try {
            val snapshot = contactsCollection(uid)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            snapshot.count.toInt()
        } catch (_: Exception) {
            0
        }
    }
}
