package com.bhushan.datasync.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.UserRepository
import com.bhushan.datasync.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val usersState: StateFlow<Resource<List<User>>> = _usersState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents

    init {
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                userRepository.observeCurrentUser(uid).collect { _userState.value = it }
            }
        }
        loadAllUsers()
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { _usersState.value = it }
        }
    }

    fun toggleDevMode(enabled: Boolean) {
        val uid = authRepository.getCurrentUserId() ?: return
        val currentRole = (_userState.value as? Resource.Success)?.data?.role ?: Role.USER
        if (currentRole != Role.ADMIN) {
            viewModelScope.launch { _toastEvents.emit("Access denied. Admin role required.") }
            return
        }
        viewModelScope.launch {
            when (val result = userRepository.setDevMode(uid, enabled)) {
                is Resource.Success -> _toastEvents.emit("Development mode updated")
                is Resource.Error -> _toastEvents.emit(result.message)
                else -> Unit
            }
        }
    }
}