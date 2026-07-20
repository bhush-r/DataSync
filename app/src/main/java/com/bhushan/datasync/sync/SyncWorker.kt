//package com.bhushan.datasync.sync
//
//import android.content.Context
//import androidx.hilt.work.HiltWorker
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.google.firebase.auth.FirebaseAuth
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//
///**
// * Background/automatic sync entry point, scheduled periodically (and once
// * immediately) by [SyncScheduler]. Delegates all real work to [SyncManager]
// * so manual and automatic sync share one code path.
// *
// * If no user is signed in (e.g. the periodic job fires after logout) the
// * worker simply succeeds without doing anything -- there is nothing to sync
// * and we should not retry.
// */
//@HiltWorker
//class SyncWorker @AssistedInject constructor(
//    @Assisted context: Context,
//    @Assisted workerParams: WorkerParameters,
//    private val syncManager: SyncManager,
//    private val firebaseAuth: FirebaseAuth
//) : CoroutineWorker(context, workerParams) {
//
//    override suspend fun doWork(): Result {
//        val uid = firebaseAuth.currentUser?.uid ?: return Result.success()
//
//        return try {
//            val success = syncManager.syncAll(uid)
//            if (success) Result.success() else Result.retry()
//        } catch (e: Exception) {
//            Result.retry()
//        }
//    }
//}


package com.bhushan.datasync.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Halt if there's no runtime user session active
        val uid = firebaseAuth.currentUser?.uid ?: return Result.success()

        // Safely restrict retry execution attempts to a max ceiling of 3 attempts
        if (runAttemptCount > 3) {
            return Result.failure()
        }

        return try {
            val success = syncManager.syncAll(uid)
            if (success) {
                Result.success()
            } else {
                // If it fails, rely on WorkManager's exponential backoff configurations
                Result.retry()
            }
        } catch (e: Exception) {
            // Extract inner causes to reliably match against Firestore's raw status exception codes
            val isQuotaExceeded = e.message?.contains("Quota exceeded") == true ||
                    e.message?.contains("RESOURCE_EXHAUSTED") == true ||
                    (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED) ||
                    e.cause is FirebaseFirestoreException && (e.cause as FirebaseFirestoreException).code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED

            if (isQuotaExceeded) {
                // Stop running completely until quota resets tomorrow to protect device battery
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }
}
