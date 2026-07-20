package com.bhushan.datasync.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.domain.repository.RegisterRepository
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormState(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerRepository: RegisterRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (!validate(name, email, password, confirmPassword)) return
        viewModelScope.launch {
            registerRepository.register(name.trim(), email.trim(), password).collect { result ->
                _registerState.value = result
            }
        }
    }

    private fun validate(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        var nameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

        if (name.isBlank()) {
            nameError = "Name is required"
            isValid = false
        }
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
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }

        _formState.value = RegisterFormState(nameError, emailError, passwordError, confirmPasswordError)
        return isValid
    }
}