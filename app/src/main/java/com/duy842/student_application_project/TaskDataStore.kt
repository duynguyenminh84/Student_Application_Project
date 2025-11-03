package com.duy842.student_application_project

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// Create a private DataStore for tasks
private val Context.taskStore by preferencesDataStore("tasks_prefs")

// ---- Your Task model must match these fields ----


object TaskPrefs {
    // Each user gets their own key
    private fun keyFor(userId: Long) = stringPreferencesKey("tasks_user_$userId")

    /** Read tasks for a specific user id as a Flow<List<Task>>. */
    fun getTasks(ctx: Context, userId: Long): Flow<List<Task>> =
        ctx.taskStore.data.map { prefs ->
            val raw = prefs[keyFor(userId)] ?: "[]"
            decodeTasks(raw)
        }

    /** Save tasks for a specific user id. */
    suspend fun saveTasks(ctx: Context, userId: Long, tasks: List<Task>) {
        val json = encodeTasks(tasks)
        ctx.taskStore.edit { it[keyFor(userId)] = json }
    }

    // --------- Minimal JSON encode/decode using org.json ---------

    private fun encodeTasks(tasks: List<Task>): String {
        val arr = JSONArray()
        for (t in tasks) {
            val o = JSONObject().apply {
                put("name", t.name)
                put("category", t.category)
                put("priority", t.priority)
                put("isDone", t.isDone)
                // nullable fields: use JSONObject.NULL when null
                if (t.scheduledDay == null) put("scheduledDay", JSONObject.NULL) else put("scheduledDay", t.scheduledDay)
                if (t.scheduledHour == null) put("scheduledHour", JSONObject.NULL) else put("scheduledHour", t.scheduledHour)
            }
            arr.put(o)
        }
        return arr.toString()
    }

    private fun decodeTasks(raw: String): List<Task> {
        return try {
            val arr = JSONArray(raw)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(
                        Task(
                            name = o.optString("name", ""),
                            category = o.optString("category", "General"),
                            priority = o.optString("priority", "Medium"),
                            isDone = o.optBoolean("isDone", false),
                            scheduledDay = o.optString("scheduledDay").takeIf { it.isNotBlank() && it != "null" },
                            scheduledHour = if (o.isNull("scheduledHour")) null else o.optInt("scheduledHour")
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}