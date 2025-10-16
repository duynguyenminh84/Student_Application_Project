package com.duy842.student_application_project

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.taskDataStore by preferencesDataStore(name = "tasks")
private val TASKS_KEY = stringSetPreferencesKey("task_list")

object TaskPrefs {
    fun getTasks(context: Context): Flow<List<Task>> {
        return context.taskDataStore.data.map { prefs ->
            prefs[TASKS_KEY]?.map {
                val parts = it.split("|")
                Task(
                    name = parts.getOrNull(0) ?: "",
                    category = parts.getOrNull(1) ?: "General",
                    priority = parts.getOrNull(2) ?: "Medium",
                    isDone = parts.getOrNull(3)?.toBoolean() ?: false,
                    scheduledDay = parts.getOrNull(4),
                    scheduledHour = parts.getOrNull(5)?.toIntOrNull()
                )
            } ?: emptyList()
        }
    }

    suspend fun saveTasks(context: Context, tasks: List<Task>) {
        val taskStrings = tasks.map {
            "${it.name}|${it.category}|${it.priority}|${it.isDone}|${it.scheduledDay ?: ""}|${it.scheduledHour ?: ""}"
        }.toSet()
        context.taskDataStore.edit { prefs ->
            prefs[TASKS_KEY] = taskStrings
        }
    }
}
