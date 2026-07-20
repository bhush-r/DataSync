package com.bhushan.datasync.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background/automatic sync entry point, scheduled periodically (and once
 * immediately) by [SyncScheduler]. Delegates all real work to [SyncManager]
 * so manual and automatic sync share one code path.
 *
 * If no user is signed in (e.g. the periodic job fires after logout) the
 * worker simply succeeds without doing anything -- there is nothing to sync
 * and we should not retry.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val uid = firebaseAuth.currentUser?.uid ?: return Result.success()

        return try {
            val success = syncManager.syncAll(uid)
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
