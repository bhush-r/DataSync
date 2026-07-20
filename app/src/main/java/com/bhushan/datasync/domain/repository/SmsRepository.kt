package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.SmsItem
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SmsRepository {

    /** Reads SMS messages straight from the device ContentResolver. Requires READ_SMS. */
    suspend fun readDeviceSms(): Resource<List<SmsItem>>

    fun observeSyncedSms(uid: String): Flow<Resource<List<SmsItem>>>

    suspend fun syncSms(uid: String, messages: List<SmsItem>): Resource<Int>

    suspend fun getSyncedCount(uid: String): Int
}
