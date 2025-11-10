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
 * AuthViewModel — pure ViewModel with injected dependencies.
 * We use SharingStarted.Eagerly so flows update even without an active collector (helps unit tests).
 */
class AuthViewModel(
    private val repo: AuthRepository,
    private val session: AuthSessionStore
) : ViewModel() {

    // Eagerly start collecting so tests don't need to subscribe explicitly
    val currentUserId: StateFlow<Long> = session.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val isLoggedIn: StateFlow<Boolean> = currentUserId
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _error.value = null
            repo.login(email, password)
                .onSuccess { user -> session.setCurrentUser(user.id) }
                .onFailure { e -> _error.value = e.message }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _error.value = "Please enter your new email password"
                return@launch
            }
            _error.value = null
            repo.register(email, password)
                .onSuccess { id -> session.setCurrentUser(id) }
                .onFailure { e -> _error.value = e.message }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _error.value = null
            session.clear()
        }
    }
}
