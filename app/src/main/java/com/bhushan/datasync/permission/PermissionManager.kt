package com.bhushan.datasync.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the three runtime permissions this app cares
 * about (Contacts, Call Log, SMS). Every screen that reads device data asks
 * this class "am I allowed?" before touching a ContentProvider, and the
 * Activities use [REQUIRED_PERMISSIONS] to drive the system permission
 * request dialog.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val PERMISSION_CONTACTS = Manifest.permission.READ_CONTACTS
        const val PERMISSION_CALL_LOG = Manifest.permission.READ_CALL_LOG
        const val PERMISSION_SMS = Manifest.permission.READ_SMS

        val REQUIRED_PERMISSIONS = arrayOf(
            PERMISSION_CONTACTS,
            PERMISSION_CALL_LOG,
            PERMISSION_SMS
        )
    }

    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasContactsPermission(): Boolean = hasPermission(PERMISSION_CONTACTS)

    fun hasCallLogPermission(): Boolean = hasPermission(PERMISSION_CALL_LOG)

    fun hasSmsPermission(): Boolean = hasPermission(PERMISSION_SMS)

    fun hasAllPermissions(): Boolean = REQUIRED_PERMISSIONS.all { hasPermission(it) }

    fun getMissingPermissions(): List<String> =
        REQUIRED_PERMISSIONS.filterNot { hasPermission(it) }
}
