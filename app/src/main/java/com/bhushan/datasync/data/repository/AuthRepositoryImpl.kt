package com.bhushan.datasync.data.repository

import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.NetworkUtils
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager,
    private val networkUtils: NetworkUtils
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)

        if (!networkUtils.isConnected()) {
            emit(Resource.Error("No internet connection. Please check your network."))
            return@flow
        }

        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: throw IllegalStateException("Authentication succeeded but no user id returned")

            // Fetch or lazily create the Firestore user profile document.
            val userDocRef = firestore.collection(Constants.COLLECTION_USERS).document(uid)
            val snapshot = userDocRef.get().await()

            val user: User = if (snapshot.exists()) {
                snapshot.toObject(User::class.java) ?: User(uid = uid, email = email)
            } else {
                val newUser = User(
                    uid = uid,
                    email = email,
                    role = Role.USER,
                    devModeEnabled = false,
                    createdAt = System.currentTimeMillis()
                )
                userDocRef.set(newUser).await()
                newUser
            }

            // Requirement #6: generate & persist FCM token right after successful login.
            val fcmToken = try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                null
            }
            if (!fcmToken.isNullOrBlank() && fcmToken != user.fcmToken) {
                userDocRef.update(Constants.FIELD_FCM_TOKEN, fcmToken).await()
            }

            sessionManager.saveSession(uid = uid, email = user.email, role = user.role)
            sessionManager.updateDevMode(user.devModeEnabled)
            sessionManager.updateLastSync(user.lastSyncAt)

            emit(Resource.Success(user.copy(fcmToken = fcmToken ?: user.fcmToken)))
        } catch (e: FirebaseAuthInvalidUserException) {
            emit(Resource.Error("No account found with this email."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            emit(Resource.Error("Incorrect email or password."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Login failed. Please try again."))
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
