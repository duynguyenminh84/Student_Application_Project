package com.duy842.student_application_project

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// Private DataStore for tasks
private val Context.taskStore by preferencesDataStore("tasks_prefs")

object TaskPrefs {

    // one key per user
    private fun keyFor(userId: Long) = stringPreferencesKey("tasks_user_$userId")

    /** Stream tasks for a specific user as Flow<List<Task>>. */
    fun getTasks(ctx: Context, userId: Long): Flow<List<Task>> =
        ctx.taskStore.data.map { prefs ->
            val raw = prefs[keyFor(userId)] ?: "[]"
            decodeTasks(raw)
        }

    /**
     * Save tasks for a specific user:
     * 1) write to local DataStore
     * 2) best-effort mirror to Firestore (won’t crash UI if it fails)
     */
    suspend fun saveTasks(ctx: Context, userId: Long, tasks: List<Task>) {
        val json = encodeTasks(tasks)
        ctx.taskStore.edit { it[keyFor(userId)] = json }

        // Mirror to Firestore (optional; safe to ignore failures)
        try {
            CloudTasks.pushAll(userId, tasks)
        } catch (_: Exception) {
            // You could log this if you want; we intentionally don't throw.
        }
    }

    // ---------- Minimal JSON encode/decode (org.json) ----------

    private fun encodeTasks(tasks: List<Task>): String {
        val arr = JSONArray()
        for (t in tasks) {
            val o = JSONObject().apply {
                put("name", t.name)
                put("category", t.category)
                put("priority", t.priority)
                put("isDone", t.isDone)
                if (t.scheduledDay == null) put("scheduledDay", JSONObject.NULL) else put("scheduledDay", t.scheduledDay)
                if (t.scheduledHour == null) put("scheduledHour", JSONObject.NULL) else put("scheduledHour", t.scheduledHour)
            }
            arr.put(o)
        }
        return arr.toString()
    }

    private fun decodeTasks(raw: String): List<Task> = try {
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
                        scheduledDay = o.optString("scheduledDay")
                            .takeIf { it.isNotBlank() && it != "null" },
                        scheduledHour = if (o.isNull("scheduledHour")) null else o.optInt("scheduledHour")
                    )
                )
            }
        }
    } catch (_: Throwable) {
        emptyList()
    }
}
