package com.bhushan.datasync.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.UserRepository
import com.bhushan.datasync.sync.SyncScheduler
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _currentUser = MutableStateFlow<Resource<User>>(Resource.Loading)
    val currentUser: StateFlow<Resource<User>> = _currentUser.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        observeProfile()
    }

    private fun observeProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.observeCurrentUser(uid).collect { resource ->
                _currentUser.value = resource
                if (resource is Resource.Success) {
                    sessionManager.updateRole(resource.data.role)
                    sessionManager.updateDevMode(resource.data.devModeEnabled)
                }
            }
        }
    }

    fun logout() {
        syncScheduler.cancelAllSync()
        authRepository.logout()
        viewModelScope.launch { sessionManager.clearSession() }
    }
}
