package com.duy842.student_application_project.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duy842.student_application_project.data.auth.AuthRepository
import com.duy842.student_application_project.data.auth.AuthSessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * AuthViewModel — contains the authentication logic for the UI.
 * Uses AuthRepository for data access and AuthSessionStore for storing current user id.
 */
class AuthViewModel(
    private val repo: AuthRepository,
    private val session: AuthSessionStore
) : ViewModel() {

    // Current user id from the session
    val currentUserId: StateFlow<Long> = session.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    // User is considered logged in if uid > 0
    val isLoggedIn: StateFlow<Boolean> = currentUserId
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                _error.value = "Please enter your email and password"
                return@launch
            }

            _error.value = null

            repo.login(trimmedEmail, trimmedPassword)
                .onSuccess { user ->
                    // Store user id in session – this drives isLoggedIn and the rest of the app
                    session.setCurrentUser(user.id)
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Login failed. Please check your email and password."
                }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                _error.value = "Please enter your new email and password"
                return@launch
            }

            _error.value = null

            repo.register(trimmedEmail, trimmedPassword)
                .onSuccess { id ->
                    // After successful registration, log the user in by saving the id
                    session.setCurrentUser(id)
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Registration failed. Please try again."
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _error.value = null
            session.clear()
        }
    }
}
