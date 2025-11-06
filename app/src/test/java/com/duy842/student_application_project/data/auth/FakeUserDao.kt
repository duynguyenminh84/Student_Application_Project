package com.duy842.student_application_project.data.auth

/**
 * In-memory UserDao implementation for unit tests.
 * No Android/Room runtime needed.
 */
class FakeUserDao : UserDao {
    private val usersById = mutableMapOf<Long, UserEntity>()
    private val idByEmail = mutableMapOf<String, Long>()
    private var nextId = 1L

    override suspend fun insert(user: UserEntity): Long {
        val normalized = user.email.trim().lowercase()
        if (idByEmail.containsKey(normalized)) {
            // Mimic unique index violation (OnConflictStrategy.ABORT)
            throw IllegalStateException("Email already exists: $normalized")
        }
        val id = nextId++
        val entity = user.copy(id = id, email = normalized)
        usersById[id] = entity
        idByEmail[normalized] = id
        return id
    }

    override suspend fun findByEmail(email: String): UserEntity? {
        val normalized = email.trim().lowercase()
        val id = idByEmail[normalized] ?: return null
        return usersById[id]
    }

    override suspend fun count(): Int = usersById.size
}
