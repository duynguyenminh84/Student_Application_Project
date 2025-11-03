package com.duy842.student_application_project.data.auth

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(email: String, password: String): Result<Long> {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }
        val existing = userDao.findByEmail(normalized)
        if (existing != null) return Result.failure(IllegalStateException("Email already in use"))

        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)
        val id = userDao.insert(
            UserEntity(email = normalized, passwordHash = hash, salt = salt)
        )
        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val normalized = email.trim().lowercase()
        val user = userDao.findByEmail(normalized)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))
        return if (PasswordHasher.verify(password, user.salt, user.passwordHash))
            Result.success(user)
        else
            Result.failure(IllegalArgumentException("Invalid email or password"))
    }
}
