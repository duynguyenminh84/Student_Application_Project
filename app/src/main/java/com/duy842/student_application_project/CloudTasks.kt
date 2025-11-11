package com.duy842.student_application_project

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object CloudTasks {

    private fun userTasks(uid: Long) =
        Firebase.firestore.collection("users")
            .document(uid.toString())
            .collection("tasks")

    // Push the whole list (simple mirror): clear then batch write
    suspend fun pushAll(uid: Long, tasks: List<Task>) {
        val col = userTasks(uid)
        val existing = col.get().await()
        val batch = Firebase.firestore.batch()
        // delete old
        existing.documents.forEach { batch.delete(it.reference) }
        // write new
        tasks.forEach { t ->
            val doc = col.document() // or a stable id if you have one
            batch.set(doc, mapOf(
                "name" to t.name,
                "category" to t.category,
                "priority" to t.priority,
                "isDone" to t.isDone,
                "scheduledDay" to t.scheduledDay,
                "scheduledHour" to t.scheduledHour
            ))
        }
        batch.commit().await()
    }

    // Pull (one shot). Returns null if nothing in cloud.
    suspend fun pullAll(uid: Long): List<Task>? {
        val snap = userTasks(uid).get().await()
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
