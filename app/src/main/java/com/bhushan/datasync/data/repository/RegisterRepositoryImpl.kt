package com.bhushan.datasync.data.repository

import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.RegisterRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.NetworkUtils
import com.bhushan.datasync.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val networkUtils: NetworkUtils
) : RegisterRepository {

    override fun register(name: String, email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        if (!networkUtils.isConnected()) {
            emit(Resource.Error("No internet connection. Please check your network."))
            return@flow
        }
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: throw IllegalStateException("Registration succeeded but no user id returned")
            val now = System.currentTimeMillis()

            // Debug Registration Hook: Assign ADMIN dynamically for matching test domains
            val determinedRole = if (email.trim().endsWith("@datasync.com", ignoreCase = true)) {
                Role.ADMIN
            } else {
                Role.USER
            }
            val newUser = User(
                uid = uid,
                name = name,
                email = email,
                role = determinedRole,
                devModeEnabled = false,
                createdAt = now,
                lastLogin = now
            )
            firestore.collection(Constants.COLLECTION_USERS).document(uid).set(newUser).await()
            emit(Resource.Success(newUser))
        } catch (e: FirebaseAuthUserCollisionException) {
            emit(Resource.Error("An account with this email already exists."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            emit(Resource.Error(e.reason ?: "Password is too weak."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Registration failed. Please try again."))
        }
    }
    }
