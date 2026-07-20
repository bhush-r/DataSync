package com.bhushan.datasync.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.bhushan.datasync.R
import com.bhushan.datasync.domain.repository.UserRepository
import com.bhushan.datasync.sync.SyncScheduler
import com.bhushan.datasync.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DataSyncFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var syncScheduler: SyncScheduler

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = firebaseAuth.currentUser?.uid ?: return
        serviceScope.launch {
            userRepository.updateFcmToken(uid, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Remote Sync Execution Command Hook
        if (message.data[Constants.FCM_DATA_ACTION_KEY] == Constants.FCM_DATA_ACTION_SYNC) {
            syncScheduler.triggerOneTimeSync()
        }

        val title = message.notification?.title ?: getString(R.string.app_name)
        val body = message.notification?.body ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_sync)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.NOTIFICATION_ID_SYNC, notification)
    }
}