package com.duy842.student_application_project.data.auth



import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Small abstraction over session storage so the ViewModel
 * can be tested with a fake (no Android, no Robolectric).
 */
interface AuthSessionStore {
    val currentUserId: Flow<Long>
    suspend fun setCurrentUser(id: Long)
    suspend fun clear()
}

/**
 * Real adapter backed by DataStore via AuthPrefs.
 * Used in the running app; tests will use a fake implementation.
 */
class AuthPrefsStore(private val ctx: Context) : AuthSessionStore {
    override val currentUserId: Flow<Long> = AuthPrefs.currentUserId(ctx)
    override suspend fun setCurrentUser(id: Long) = AuthPrefs.setCurrentUser(ctx, id)
    override suspend fun clear() = AuthPrefs.clear(ctx)
}
