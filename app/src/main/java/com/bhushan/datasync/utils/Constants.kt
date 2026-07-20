package com.bhushan.datasync.utils

/**
 * Centralized constant values used across the app. Keeping every "magic
 * string" here avoids typos when the same Firestore collection / field name
 * is referenced from multiple repositories.
 */
object Constants {

    // ---- Firestore top level collections -------------------------------
    const val COLLECTION_USERS = "users"
    const val SUBCOLLECTION_CONTACTS = "contacts"
    const val SUBCOLLECTION_CALL_LOGS = "callLogs"
    const val SUBCOLLECTION_SMS = "sms"
    const val SUBCOLLECTION_RECORDS = "records"
    const val COLLECTION_APP_CONFIG = "app_config"
    const val DOC_GLOBAL_CONFIG = "global"

    // ---- Firestore user document fields ---------------------------------
    const val FIELD_EMAIL = "email"
    const val FIELD_ROLE = "role"
    const val FIELD_FCM_TOKEN = "fcmToken"
    const val FIELD_LAST_SYNC_AT = "lastSyncAt"
    const val FIELD_DEV_MODE_ENABLED = "devModeEnabled"
    const val FIELD_CREATED_AT = "createdAt"

    // ---- DataStore / SharedPreferences keys -----------------------------
    const val PREFS_NAME = "datasync_session_prefs"
    const val KEY_USER_UID = "key_user_uid"
    const val KEY_USER_EMAIL = "key_user_email"
    const val KEY_USER_ROLE = "key_user_role"
    const val KEY_DEV_MODE = "key_dev_mode"
    const val KEY_LAST_SYNC_AT = "key_last_sync_at"
    const val KEY_IS_LOGGED_IN = "key_is_logged_in"

    // ---- WorkManager -----------------------------------------------------
    const val WORK_NAME_PERIODIC_SYNC = "com.bhushan.datasync.PERIODIC_SYNC_WORK"
    const val WORK_NAME_ONE_TIME_SYNC = "com.bhushan.datasync.ONE_TIME_SYNC_WORK"
    const val SYNC_INTERVAL_MINUTES = 15L

    // ---- Notifications -----------------------------------------------------
    const val NOTIFICATION_CHANNEL_ID = "datasync_channel"
    const val NOTIFICATION_CHANNEL_NAME = "DataSync Notifications"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Sync status and app notifications"
    const val NOTIFICATION_ID_SYNC = 1001

    // ---- Roles -----------------------------------------------------------
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_USER = "USER"

    // ---- Call log filter values -----------------------------------------
    const val FILTER_ALL = "ALL"
    const val FILTER_INCOMING = "INCOMING"
    const val FILTER_OUTGOING = "OUTGOING"
    const val FILTER_MISSED = "MISSED"

    // ---- Misc -----------------------------------------------------------
    const val SEARCH_DEBOUNCE_MS = 300L
    const val SMS_PREVIEW_MAX_LENGTH = 80
}
