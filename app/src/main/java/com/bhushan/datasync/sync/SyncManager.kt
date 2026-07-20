package com.bhushan.datasync.sync

import com.bhushan.datasync.domain.model.SyncState
import com.bhushan.datasync.domain.repository.CallLogRepository
import com.bhushan.datasync.domain.repository.ContactRepository
import com.bhushan.datasync.domain.repository.SmsRepository
import com.bhushan.datasync.domain.repository.UserRepository
import com.bhushan.datasync.permission.PermissionManager
import com.bhushan.datasync.utils.NetworkUtils
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single engine behind BOTH the manual "Sync Now" button and the
 * background [SyncWorker]. Keeping the orchestration logic in one class
 * guarantees identical behaviour (permission checks, duplicate prevention,
 * error handling) no matter which trigger path invoked it.
 *
 * Progress/result is broadcast through [syncState] so the Dashboard and any
 * currently visible list screen can react live, satisfying the
 * "display synchronization progress and status" requirement.
 */
@Singleton
class SyncManager @Inject constructor(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val smsRepository: SmsRepository,
    private val userRepository: UserRepository,
    private val permissionManager: PermissionManager,
    private val networkUtils: NetworkUtils
) {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /**
     * Runs a full sync pass (Contacts -> Call Logs -> SMS) for [uid].
     * Returns true on full success, false if any stage failed.
     */
    suspend fun syncAll(uid: String): Boolean {
        if (!networkUtils.isConnected()) {
            _syncState.value = SyncState.Error("No internet connection. Please check your network.")
            return false
        }

        var overallSuccess = true

        // ---- Contacts ------------------------------------------------
        if (permissionManager.hasContactsPermission()) {
            _syncState.value = SyncState.Syncing("Reading contacts", 10)
            when (val deviceContacts = contactRepository.readDeviceContacts()) {
                is Resource.Success -> {
                    _syncState.value = SyncState.Syncing("Uploading contacts", 30)
                    val result = contactRepository.syncContacts(uid, deviceContacts.data)
                    if (result is Resource.Error) overallSuccess = false
                }
                is Resource.Error -> overallSuccess = false
                else -> Unit
            }
        }

        // ---- Call Logs -------------------------------------------------
        if (permissionManager.hasCallLogPermission()) {
            _syncState.value = SyncState.Syncing("Reading call logs", 45)
            when (val deviceLogs = callLogRepository.readDeviceCallLogs()) {
                is Resource.Success -> {
                    _syncState.value = SyncState.Syncing("Uploading call logs", 65)
                    val result = callLogRepository.syncCallLogs(uid, deviceLogs.data)
                    if (result is Resource.Error) overallSuccess = false
                }
                is Resource.Error -> overallSuccess = false
                else -> Unit
            }
        }

        // ---- SMS ---------------------------------------------------------
        if (permissionManager.hasSmsPermission()) {
            _syncState.value = SyncState.Syncing("Reading messages", 80)
            when (val deviceSms = smsRepository.readDeviceSms()) {
                is Resource.Success -> {
                    _syncState.value = SyncState.Syncing("Uploading messages", 95)
                    val result = smsRepository.syncSms(uid, deviceSms.data)
                    if (result is Resource.Error) overallSuccess = false
                }
                is Resource.Error -> overallSuccess = false
                else -> Unit
            }
        }

        val now = System.currentTimeMillis()
        if (overallSuccess) {
            userRepository.updateLastSyncTimestamp(uid, now)
            _syncState.value = SyncState.Success(now)
        } else {
            _syncState.value = SyncState.Error("Some data failed to sync. Tap to retry.")
        }
        return overallSuccess
    }

    fun resetState() {
        _syncState.value = SyncState.Idle
    }
}
