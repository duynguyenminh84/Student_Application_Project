package com.duy842.student_application_project.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duy842.student_application_project.data.AppDatabase
import com.duy842.student_application_project.data.auth.AuthPrefs
import com.duy842.student_application_project.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val repo = AuthRepository(db.userDao())

    // observe current user id from prefs
    val currentUserId: StateFlow<Long> = AuthPrefs.currentUserId(app).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), -1L
    )
    val isLoggedIn: StateFlow<Boolean> = currentUserId.map { it > 0 }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _error.value = null
            repo.login(email, password)
                .onSuccess { user ->
                    AuthPrefs.setCurrentUser(getApplication(), user.id)
                }
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
                .onSuccess { id ->
                    AuthPrefs.setCurrentUser(getApplication(), id)
                }
                .onFailure { e -> _error.value = e.message }
        }
    }

    fun logout() {
        viewModelScope.launch { AuthPrefs.clear(getApplication()) }
    }
}
