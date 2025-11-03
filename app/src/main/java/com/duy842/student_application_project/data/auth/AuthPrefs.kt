package com.duy842.student_application_project.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore("auth_prefs")

object AuthPrefs {
    private val KEY_UID = longPreferencesKey("current_user_id")

    /** Current user id stream; -1L means logged out */
    fun currentUserId(ctx: Context): Flow<Long> =
        ctx.authDataStore.data.map { it[KEY_UID] ?: -1L }

    /** Set the logged-in user id */
    suspend fun setCurrentUser(ctx: Context, id: Long) {
        ctx.authDataStore.edit { it[KEY_UID] = id }
    }

    /** Clear login (logout) */
    suspend fun clear(ctx: Context) {
        ctx.authDataStore.edit { it.remove(KEY_UID) }
    }
}
