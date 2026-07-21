package com.bhushan.datasync.service

import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CallLog
import android.provider.Telephony
import com.bhushan.datasync.sync.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DataSyncObserverService : Service() {

    @Inject
    lateinit var syncScheduler: SyncScheduler

    private val handler = Handler(Looper.getMainLooper())
    private var syncRunnable: Runnable? = null
    private val DEBOUNCE_DELAY_MS = 5000L // Wait 5 seconds before executing sync

    private fun scheduleDebouncedSync() {
        syncRunnable?.let { handler.removeCallbacks(it) }
        syncRunnable = Runnable {
            syncScheduler.triggerOneTimeSync()
        }
        handler.postDelayed(syncRunnable!!, DEBOUNCE_DELAY_MS)
    }

    private val callLogObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            scheduleDebouncedSync()
        }
    }

    private val smsObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            scheduleDebouncedSync()
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver)
            contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver)
        } catch (_: SecurityException) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        syncRunnable?.let { handler.removeCallbacks(it) }
        contentResolver.unregisterContentObserver(callLogObserver)
        contentResolver.unregisterContentObserver(smsObserver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}