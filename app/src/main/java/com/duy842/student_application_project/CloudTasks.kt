package com.duy842.student_application_project

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Cloud mirror for tasks.
 * Use a stable string key for the user (e.g., email or uid.toString()) so
 * local <-> cloud mapping stays consistent across devices.
 */
object CloudTasks {

    private fun userTasks(cloudKey: String) =
        Firebase.firestore
            .collection("users")
            .document(cloudKey)
            .collection("tasks")

    /**
     * Replace all remote tasks with the given list (simple mirror).
     * Safe, idempotent “clear then batch write”.
     */
    suspend fun pushAll(cloudKey: String, tasks: List<Task>) {
        val col = userTasks(cloudKey)
        val existing = col.get().await()

        val batch = Firebase.firestore.batch()
        // delete old
        existing.documents.forEach { batch.delete(it.reference) }
        // write new
        tasks.forEach { t ->
            val doc = col.document() // if you add stable IDs, replace this with doc(t.id)
            batch.set(
                doc,
                mapOf(
                    "name" to t.name,
                    "category" to t.category,
                    "priority" to t.priority,
                    "isDone" to t.isDone,
                    "scheduledDay" to t.scheduledDay,
                    "scheduledHour" to t.scheduledHour
                )
            )
        }
        batch.commit().await()
    }

    /**
     * One-shot fetch of all remote tasks for this user.
     * Returns null if no tasks are present.
     */
    suspend fun pullAll(cloudKey: String): List<Task>? {
        val snap = userTasks(cloudKey).get().await()
        if (snap.isEmpty) return null
        return snap.documents.map { d ->
            Task(
                name = d.getString("name") ?: "",
                category = d.getString("category") ?: "General",
                priority = d.getString("priority") ?: "Medium",
                isDone = d.getBoolean("isDone") ?: false,
                scheduledDay = d.getString("scheduledDay"),
                scheduledHour = d.getLong("scheduledHour")?.toInt()
            )
        }
    }
}
