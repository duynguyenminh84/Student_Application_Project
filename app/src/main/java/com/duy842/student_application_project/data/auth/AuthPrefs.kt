package com.duy842.student_application_project.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

object AuthPrefs {
    private val KEY_USER_ID = longPreferencesKey("current_user_id")

    fun currentUserId(context: Context) =
        context.dataStore.data.map { it[KEY_USER_ID] ?: -1L }

    suspend fun setCurrentUser(context: Context, id: Long) {
        context.dataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.remove(KEY_USER_ID) }
    }
}
