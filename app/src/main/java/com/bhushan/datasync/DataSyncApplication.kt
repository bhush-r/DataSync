package com.bhushan.datasync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.bhushan.datasync.sync.SyncScheduler
import com.bhushan.datasync.utils.Constants
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 *
 * - Bootstraps Hilt dependency graph for the whole app.
 * - Provides a custom [Configuration] so WorkManager can create workers that
 *   require constructor injection (Hilt-Work integration).
 * - Creates the notification channel used for sync status / FCM notifications.
 * - Schedules the recurring background sync job on first launch of the process.
 */
@HiltAndroidApp
class DataSyncApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Ensure periodic background sync is (re)scheduled every time the
        // process is created. WorkManager de-dupes by unique work name so
        // this is a cheap no-op if already scheduled.
        syncScheduler.schedulePeriodicSync()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = Constants.NOTIFICATION_CHANNEL_DESCRIPTION
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
