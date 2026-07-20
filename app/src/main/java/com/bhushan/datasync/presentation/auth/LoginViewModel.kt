package com.bhushan.datasync.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.sync.SyncScheduler
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginFormState(
    val emailError: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validate(email, password)) return

        viewModelScope.launch {
            authRepository.login(email.trim(), password).collect { result ->
                _loginState.value = result
                if (result is Resource.Success) {
                    // Kick off an immediate sync + guarantee the recurring
                    // background job is scheduled the moment a session starts.
                    syncScheduler.schedulePeriodicSync()
                    syncScheduler.triggerOneTimeSync()
                }
            }
        }
    }

    private fun validate(email: String, password: String): Boolean {
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!email.isValidEmail()) {
            emailError = "Enter a valid email address"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        }

        _formState.value = LoginFormState(emailError, passwordError)
        return isValid
    }
}
