package com.bhushan.datasync.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bhushan.datasync.utils.Constants
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules [SyncWorker] with WorkManager -- this is what satisfies
 * requirement #27 (automatic background sync) and #26 in its "one-time,
 * triggered from a Sync button" flavour.
 *
 * Both request types require a validated network connection
 * ([NetworkType.CONNECTED]) so a job never wastes battery only to fail on
 * "no internet".
 */
@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Enqueues a recurring background sync every [Constants.SYNC_INTERVAL_MINUTES]. */
    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            Constants.SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_NAME_PERIODIC_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Enqueues an immediate, one-off sync -- used as a fallback trigger from the UI. */
    fun triggerOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            Constants.WORK_NAME_ONE_TIME_SYNC,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelAllSync() {
        workManager.cancelUniqueWork(Constants.WORK_NAME_PERIODIC_SYNC)
        workManager.cancelUniqueWork(Constants.WORK_NAME_ONE_TIME_SYNC)
    }
}
