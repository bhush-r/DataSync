package com.bhushan.datasync.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bhushan.datasync.sync.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * WorkManager's periodic jobs already survive reboots on modern Android
 * versions, but explicitly re-enqueueing here guards against edge cases on
 * OEM ROMs that aggressively clear scheduled jobs on boot.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            syncScheduler.schedulePeriodicSync()
        }
    }
}
