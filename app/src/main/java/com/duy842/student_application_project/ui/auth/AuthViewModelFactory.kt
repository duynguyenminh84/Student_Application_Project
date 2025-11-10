package com.duy842.student_application_project.ui.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duy842.student_application_project.data.AppDatabase
import com.duy842.student_application_project.data.auth.AuthPrefsStore
import com.duy842.student_application_project.data.auth.AuthRepository

/**
 * Provides the real dependencies for AuthViewModel at runtime:
 *  - Room UserDao via AppDatabase
 *  - AuthRepository
 *  - AuthPrefsStore (DataStore-backed session)
 */
class AuthViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(app)
        val repo = AuthRepository(db.userDao())
        val session = AuthPrefsStore(app)
        return AuthViewModel(repo, session) as T
    }
}
