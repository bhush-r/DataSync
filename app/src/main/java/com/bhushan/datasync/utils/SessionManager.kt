package com.bhushan.datasync.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bhushan.datasync.domain.model.Role
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

/**
 * Local, offline-first cache of the authenticated user's session state.
 *
 * Firebase Auth already persists the signed-in user across process death,
 * but the machine test additionally requires the app to remember the user's
 * ROLE and DEVELOPMENT MODE flag instantly on cold start -- before the first
 * Firestore round-trip completes. This class is the single source of truth
 * for that cached state and is what [BaseActivity] consults to decide
 * whether a screen is reachable at all (session guard / route protection).
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val UID = stringPreferencesKey(Constants.KEY_USER_UID)
        val EMAIL = stringPreferencesKey(Constants.KEY_USER_EMAIL)
        val ROLE = stringPreferencesKey(Constants.KEY_USER_ROLE)
        val DEV_MODE = booleanPreferencesKey(Constants.KEY_DEV_MODE)
        val LAST_SYNC = longPreferencesKey(Constants.KEY_LAST_SYNC_AT)
        val LOGGED_IN = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    }

    val isLoggedInFlow: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.LOGGED_IN] ?: false }

    val roleFlow: Flow<Role> =
        context.dataStore.data.map { Role.fromString(it[Keys.ROLE]) }

    val devModeFlow: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DEV_MODE] ?: false }

    val lastSyncFlow: Flow<Long> =
        context.dataStore.data.map { it[Keys.LAST_SYNC] ?: 0L }

    suspend fun isLoggedIn(): Boolean = isLoggedInFlow.first()

    suspend fun getRole(): Role = roleFlow.first()

    suspend fun getCachedUid(): String? =
        context.dataStore.data.map { it[Keys.UID] }.first()

    suspend fun saveSession(uid: String, email: String, role: Role) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UID] = uid
            prefs[Keys.EMAIL] = email
            prefs[Keys.ROLE] = role.name
            prefs[Keys.LOGGED_IN] = true
        }
    }

    suspend fun updateRole(role: Role) {
        context.dataStore.edit { prefs -> prefs[Keys.ROLE] = role.name }
    }

    suspend fun updateDevMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DEV_MODE] = enabled }
    }

    suspend fun updateLastSync(timestampMillis: Long) {
        context.dataStore.edit { prefs -> prefs[Keys.LAST_SYNC] = timestampMillis }
    }

    /** Clears all cached session data. Called on logout. */
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
