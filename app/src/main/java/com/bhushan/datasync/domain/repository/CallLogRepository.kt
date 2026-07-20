package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.CallLogItem
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {

    /** Reads call logs straight from the device ContentResolver. Requires READ_CALL_LOG. */
    suspend fun readDeviceCallLogs(): Resource<List<CallLogItem>>

    fun observeSyncedCallLogs(uid: String): Flow<Resource<List<CallLogItem>>>

    suspend fun syncCallLogs(uid: String, callLogs: List<CallLogItem>): Resource<Int>

    suspend fun getSyncedCount(uid: String): Int
}
